package com.am.blog.controller;

import com.am.blog.entity.Blog;
import com.am.blog.entity.BlogCategory;
import com.am.blog.entity.BlogTag;
import com.am.blog.service.BlogService;
import com.am.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "博客管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    // ===== 公开接口 =====

    @Operation(summary = "博客列表(公开)")
    @GetMapping("/blogs")
    public Result<Page<Blog>> listBlogs(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) Long tagId) {
        return Result.success(blogService.listBlogs(pageNum, pageSize, categoryId, tagId, true));
    }

    @Operation(summary = "博客详情(公开)")
    @GetMapping("/blogs/{id}")
    public Result<Blog> getBlogDetail(@PathVariable Long id) {
        return Result.success(blogService.getBlogDetail(id));
    }

    @Operation(summary = "最近博客(公开)")
    @GetMapping("/blogs/recent")
    public Result<Page<Blog>> getRecentBlogs(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(blogService.getRecentBlogs(limit));
    }

    @Operation(summary = "分类列表(公开)")
    @GetMapping("/blogs/categories")
    public Result<List<BlogCategory>> listCategories() {
        return Result.success(blogService.listCategories());
    }

    @Operation(summary = "标签列表(公开)")
    @GetMapping("/blogs/tags")
    public Result<List<BlogTag>> listTags() {
        return Result.success(blogService.listTags());
    }

    // ===== 管理员接口 =====

    @Operation(summary = "博客列表(管理员)")
    @GetMapping("/admin/blogs")
    public Result<Page<Blog>> listAllBlogs(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(blogService.listBlogs(pageNum, pageSize, null, null, false));
    }

    @Operation(summary = "新增博客(管理员)")
    @PostMapping("/admin/blogs")
    public Result<Void> addBlog(@RequestBody Blog blog, @RequestParam(required = false) List<Long> tagIds) {
        blogService.addBlog(blog, tagIds);
        return Result.success();
    }

    @Operation(summary = "修改博客(管理员)")
    @PutMapping("/admin/blogs")
    public Result<Void> updateBlog(@RequestBody Blog blog, @RequestParam(required = false) List<Long> tagIds) {
        blogService.updateBlog(blog, tagIds);
        return Result.success();
    }

    @Operation(summary = "删除博客(管理员)")
    @DeleteMapping("/admin/blogs/{id}")
    public Result<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return Result.success();
    }

    @Operation(summary = "新增分类(管理员)")
    @PostMapping("/admin/blogs/categories")
    public Result<Void> addCategory(@RequestBody BlogCategory category) {
        blogService.addCategory(category);
        return Result.success();
    }

    @Operation(summary = "删除分类(管理员)")
    @DeleteMapping("/admin/blogs/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        blogService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "新增标签(管理员)")
    @PostMapping("/admin/blogs/tags")
    public Result<Void> addTag(@RequestBody BlogTag tag) {
        blogService.addTag(tag);
        return Result.success();
    }

    @Operation(summary = "删除标签(管理员)")
    @DeleteMapping("/admin/blogs/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        blogService.deleteTag(id);
        return Result.success();
    }
}
