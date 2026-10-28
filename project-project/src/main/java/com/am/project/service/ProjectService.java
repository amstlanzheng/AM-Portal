package com.am.project.service;

import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.project.entity.Project;
import com.am.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {

    public Page<Project> listProjects(int pageNum, int pageSize, Boolean visibleOnly) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (visibleOnly != null && visibleOnly) {
            wrapper.eq(Project::getIsVisible, true);
        }
        wrapper.orderByDesc(Project::getIsTop)
               .orderByDesc(Project::getPublishTime);
        Page<Project> result = page(new Page<>(pageNum, pageSize), wrapper);
        result.getRecords().forEach(this::normalizeUrls);
        return result;
    }

    public Project getProjectDetail(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        normalizeUrls(project);
        return project;
    }

    public void addProject(Project project) {
        save(project);
    }

    public void updateProject(Project project) {
        Project existing = getById(project.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        updateById(project);
    }

    public void deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        removeById(id);
    }

    public void setTop(Long id, Boolean isTop) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        project.setIsTop(isTop);
        updateById(project);
    }

    public void setVisible(Long id, Boolean isVisible) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        project.setIsVisible(isVisible);
        updateById(project);
    }

    public Page<Project> getRecentProjects(int limit) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getIsVisible, true)
               .orderByDesc(Project::getIsTop)
               .orderByDesc(Project::getPublishTime);
        Page<Project> result = page(new Page<>(1, limit), wrapper);
        result.getRecords().forEach(this::normalizeUrls);
        return result;
    }

    /**
     * 补全外链协议头：用户填写 github.com/xxx 这类无协议地址时，
     * 前端直接跳转会被当作相对路径，这里统一补全为 https://
     */
    private void normalizeUrls(Project project) {
        project.setGithubUrl(normalizeUrl(project.getGithubUrl()));
        project.setGiteeUrl(normalizeUrl(project.getGiteeUrl()));
        project.setDemoUrl(normalizeUrl(project.getDemoUrl()));
        project.setDownloadUrl(normalizeUrl(project.getDownloadUrl()));
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String trimmed = url.trim();
        if (trimmed.matches("(?i)^(https?|mailto|tel):.*")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }
}
