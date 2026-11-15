package main.java.com.blog.controller;

import com.blog.dto.*;
import com.blog.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/folders")
    public Result<List<FavoriteFolderDTO>> getFolders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(favoriteService.getFolders(userId));
    }

    @PostMapping("/folders")
    public Result<FavoriteFolderDTO> createFolder(@Valid @RequestBody CreateFolderRequest req,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("created", favoriteService.createFolder(userId, req.getName()));
    }

    @PutMapping("/folders/{id}")
    public Result<FavoriteFolderDTO> renameFolder(@PathVariable Long id,
                                                  @Valid @RequestBody RenameFolderRequest req,
                                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("renamed", favoriteService.renameFolder(userId, id, req.getName()));
    }

    @DeleteMapping("/folders/{id}")
    public Result<Void> deleteFolder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.deleteFolder(userId, id);
        return Result.success("deleted", null);
    }

    @GetMapping("/folders/{id}/items")
    public Result<List<FavoriteItemDTO>> getFolderItems(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(favoriteService.getItemsByFolder(userId, id));
    }

    @PostMapping("/items")
    public Result<Void> addFavorite(@Valid @RequestBody AddFavoriteRequest req,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.addItem(userId, req.getFolderId(), req.getItemType(), req.getItemId(), req.getLearningStatus());
        return Result.success("saved", null);
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> removeFavorite(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.removeItem(userId, id);
        return Result.success("removed", null);
    }
}
