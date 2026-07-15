package com.am.blog.service;

import com.am.blog.entity.Blog;
import com.am.blog.entity.BlogCategory;
import com.am.blog.entity.BlogTag;
import com.am.blog.entity.BlogTagRelation;
import com.am.blog.mapper.BlogCategoryMapper;
import com.am.blog.mapper.BlogMapper;
import com.am.blog.mapper.BlogTagMapper;
import com.am.blog.mapper.BlogTagRelationMapper;
import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService extends ServiceImpl<BlogMapper, Blog> {

    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final BlogTagRelationMapper tagRelationMapper;

    public Page<Blog> listBlogs(int pageNum, int pageSize, Long categoryId, Long tagId, Boolean publishedOnly) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        if (publishedOnly != null && publishedOnly) {
            wrapper.eq(Blog::getIsPublished, true);
        }
        if (categoryId != null) {
            wrapper.eq(Blog::getCategoryId, categoryId);
        }
        if (tagId != null) {
            // 通过标签关联查询
            List<BlogTagRelation> relations = tagRelationMapper.selectList(
                    new LambdaQueryWrapper<BlogTagRelation>().eq(BlogTagRelation::getTagId, tagId));
            List<Long> blogIds = relations.stream().map(BlogTagRelation::getBlogId).toList();
            if (blogIds.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            wrapper.in(Blog::getId, blogIds);
        }
        wrapper.orderByDesc(Blog::getPublishTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public Blog getBlogDetail(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        // 增加阅读量
        blog.setViewCount(blog.getViewCount() + 1);
        updateById(blog);
        return blog;
    }

    @Transactional
    public void addBlog(Blog blog, List<Long> tagIds) {
        save(blog);
        saveTagRelations(blog.getId(), tagIds);
    }

    @Transactional
    public void updateBlog(Blog blog, List<Long> tagIds) {
        Blog existing = getById(blog.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        updateById(blog);
        // 删除旧的标签关联
        tagRelationMapper.delete(new LambdaQueryWrapper<BlogTagRelation>()
                .eq(BlogTagRelation::getBlogId, blog.getId()));
        saveTagRelations(blog.getId(), tagIds);
    }

    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        removeById(id);
        tagRelationMapper.delete(new LambdaQueryWrapper<BlogTagRelation>()
                .eq(BlogTagRelation::getBlogId, id));
    }

    public Page<Blog> getRecentBlogs(int limit) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blog::getIsPublished, true)
               .orderByDesc(Blog::getPublishTime);
        return page(new Page<>(1, limit), wrapper);
    }

    // ===== 分类管理 =====

    public List<BlogCategory> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                .orderByAsc(BlogCategory::getCreateTime));
    }

    public void addCategory(BlogCategory category) {
        categoryMapper.insert(category);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // ===== 标签管理 =====

    public List<BlogTag> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>()
                .orderByDesc(BlogTag::getCreateTime));
    }

    public void addTag(BlogTag tag) {
        tagMapper.insert(tag);
    }

    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
        tagRelationMapper.delete(new LambdaQueryWrapper<BlogTagRelation>()
                .eq(BlogTagRelation::getTagId, id));
    }

    private void saveTagRelations(Long blogId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                BlogTagRelation relation = new BlogTagRelation();
                relation.setBlogId(blogId);
                relation.setTagId(tagId);
                tagRelationMapper.insert(relation);
            }
        }
    }
}
