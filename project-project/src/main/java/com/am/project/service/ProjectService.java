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
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public Project getProjectDetail(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
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
        return page(new Page<>(1, limit), wrapper);
    }
}
