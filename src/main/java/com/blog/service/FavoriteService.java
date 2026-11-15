package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.FavoriteFolderDTO;
import com.blog.dto.FavoriteItemDTO;
import com.blog.entity.Article;
import com.blog.entity.FavoriteFolder;
import com.blog.entity.FavoriteItem;
import com.blog.entity.FeaturedArticle;
import com.blog.entity.Interview;
import com.blog.repository.ArticleRepository;
import com.blog.repository.FavoriteFolderRepository;
import com.blog.repository.FavoriteItemRepository;
import com.blog.repository.FeaturedArticleRepository;
import com.blog.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Set<String> LEARNING_STATUSES = Set.of("UNREAD", "LEARNING", "MASTERED");

    private final FavoriteFolderRepository folderRepository;
    private final FavoriteItemRepository itemRepository;
    private final ArticleRepository articleRepository;
    private final FeaturedArticleRepository featuredRepository;
    private final InterviewRepository interviewRepository;

    public FavoriteService(FavoriteFolderRepository folderRepository,
                           FavoriteItemRepository itemRepository,
                           ArticleRepository articleRepository,
                           FeaturedArticleRepository featuredRepository,
                           InterviewRepository interviewRepository) {
        this.folderRepository = folderRepository;
        this.itemRepository = itemRepository;
        this.articleRepository = articleRepository;
        this.featuredRepository = featuredRepository;
        this.interviewRepository = interviewRepository;
    }

    public List<FavoriteFolderDTO> getFolders(Long userId) {
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId)
                .orderByDesc(FavoriteFolder::getUpdateTime)
                .orderByDesc(FavoriteFolder::getCreateTime);
        List<FavoriteFolder> folders = folderRepository.selectList(wrapper);

        return folders.stream().map(folder -> {
            FavoriteFolderDTO dto = toFolderDTO(folder);
            LambdaQueryWrapper<FavoriteItem> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(FavoriteItem::getFolderId, folder.getId());
            dto.setItemCount(Math.toIntExact(itemRepository.selectCount(countWrapper)));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public FavoriteFolderDTO createFolder(Long userId, String name) {
        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(normalizeFolderName(name));
        folderRepository.insert(folder);

        FavoriteFolderDTO dto = toFolderDTO(folder);
        dto.setItemCount(0);
        return dto;
    }

    @Transactional
    public FavoriteFolderDTO renameFolder(Long userId, Long folderId, String name) {
        FavoriteFolder folder = requireFolderOwner(userId, folderId);
        folder.setName(normalizeFolderName(name));
        folder.setUpdateTime(LocalDateTime.now());
        folderRepository.updateById(folder);

        FavoriteFolderDTO dto = toFolderDTO(folder);
        LambdaQueryWrapper<FavoriteItem> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItem::getFolderId, folder.getId());
        dto.setItemCount(Math.toIntExact(itemRepository.selectCount(countWrapper)));
        return dto;
    }

    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        requireFolderOwner(userId, folderId);

        LambdaQueryWrapper<FavoriteItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteItem::getFolderId, folderId);
        itemRepository.delete(wrapper);
        folderRepository.deleteById(folderId);
    }

    public List<FavoriteItemDTO> getItemsByFolder(Long userId, Long folderId) {
        requireFolderOwner(userId, folderId);

        LambdaQueryWrapper<FavoriteItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteItem::getFolderId, folderId).orderByDesc(FavoriteItem::getCreateTime);
        List<FavoriteItem> items = itemRepository.selectList(wrapper);

        return items.stream().map(item -> {
            FavoriteItemDTO dto = new FavoriteItemDTO();
            dto.setId(item.getId());
            dto.setFolderId(item.getFolderId());
            dto.setItemType(item.getItemType());
            dto.setItemId(item.getItemId());
            dto.setLearningStatus(item.getLearningStatus());
            dto.setCreateTime(item.getCreateTime());

            switch (item.getItemType()) {
                case "article":
                    Article article = articleRepository.selectById(item.getItemId());
                    if (article != null) {
                        dto.setTitle(article.getTitle());
                        dto.setCoverImage(article.getCoverImage());
                    }
                    break;
                case "featured":
                    FeaturedArticle featured = featuredRepository.selectById(item.getItemId());
                    if (featured != null) {
                        dto.setTitle(featured.getTitle());
                        dto.setUrl(featured.getUrl());
                    }
                    break;
                case "interview":
                    Interview interview = interviewRepository.selectById(item.getItemId());
                    if (interview != null) {
                        dto.setTitle(interview.getTitle());
                        dto.setCoverImage(interview.getCoverImage());
                    }
                    break;
                default:
                    break;
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addItem(Long userId, Long folderId, String itemType, Long itemId, String learningStatus) {
        requireFolderOwner(userId, folderId);

        LambdaQueryWrapper<FavoriteItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteItem::getFolderId, folderId)
                .eq(FavoriteItem::getItemType, itemType)
                .eq(FavoriteItem::getItemId, itemId);
        if (itemRepository.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("item already exists in folder");
        }

        FavoriteItem item = new FavoriteItem();
        item.setFolderId(folderId);
        item.setItemType(itemType);
        item.setItemId(itemId);
        item.setLearningStatus(normalizeLearningStatus(learningStatus));
        itemRepository.insert(item);
        touchFolder(folderId);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        FavoriteItem item = itemRepository.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("favorite item not found");
        }

        requireFolderOwner(userId, item.getFolderId());
        itemRepository.deleteById(itemId);
        touchFolder(item.getFolderId());
    }

    private FavoriteFolder requireFolderOwner(Long userId, Long folderId) {
        FavoriteFolder folder = folderRepository.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("favorite folder not found or no permission");
        }
        return folder;
    }

    private void touchFolder(Long folderId) {
        FavoriteFolder folder = new FavoriteFolder();
        folder.setId(folderId);
        folder.setUpdateTime(LocalDateTime.now());
        folderRepository.updateById(folder);
    }

    private FavoriteFolderDTO toFolderDTO(FavoriteFolder folder) {
        FavoriteFolderDTO dto = new FavoriteFolderDTO();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setCreateTime(folder.getCreateTime());
        dto.setUpdateTime(folder.getUpdateTime());
        return dto;
    }

    private String normalizeFolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("folder name is required");
        }
        return name.trim();
    }

    private String normalizeLearningStatus(String learningStatus) {
        String normalized = learningStatus == null ? "UNREAD" : learningStatus.trim().toUpperCase();
        if (!LEARNING_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("learningStatus must be UNREAD, LEARNING or MASTERED");
        }
        return normalized;
    }
}
