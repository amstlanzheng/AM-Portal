<template>
  <div class="page-container">
    <header class="page-head">
      <div>
        <p class="eyebrow">Blog</p>
        <h1 class="page-title" data-idx="02">博客</h1>
        <p class="page-sub">记录踩过的坑、想通的事，以及一些不成体系的笔记</p>
      </div>
      <span class="count">{{ total }} 篇</span>
    </header>

    <div class="blog-layout">
      <div class="blog-main">
        <div v-if="!blogs.length" class="empty-state surface">
          <span>暂无文章</span>
        </div>

        <div class="blog-list">
          <article
            v-for="blog in blogs"
            :key="blog.id"
            class="blog-card surface card-hover tech-card"
            @click="$router.push('/blogs/' + blog.id)"
          >
            <div v-if="blog.cover" class="cover blog-thumb">
              <el-image :src="blog.cover" fit="cover" />
            </div>
            <div class="blog-body">
              <h2 class="blog-title">{{ blog.title }}</h2>
              <p class="blog-desc">{{ blog.summary }}</p>
              <div class="blog-meta">
                <span>{{ blog.publishTime }}</span>
                <span class="dot">·</span>
                <span>{{ blog.viewCount }} 次阅读</span>
              </div>
            </div>
          </article>
        </div>

        <div v-if="total > 10" class="pager">
          <el-pagination
            v-model:current-page="page"
            :page-size="10"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="loadBlogs"
          />
        </div>
      </div>

      <aside class="blog-side">
        <section class="surface side-card">
          <h3 class="side-title">分类</h3>
          <div v-if="categories.length" class="cat-list">
            <button
              v-for="cat in categories"
              :key="cat.id"
              class="cat-item"
              :class="{ active: categoryId === cat.id }"
              @click="filterByCategory(cat.id)"
            >
              {{ cat.name }}
            </button>
          </div>
          <p v-else class="faint">暂无分类</p>
        </section>

        <section class="surface side-card">
          <h3 class="side-title">标签</h3>
          <div v-if="tags.length" class="chip-list">
            <button
              v-for="tag in tags"
              :key="tag.id"
              class="chip tag-btn"
              :class="{ active: tagId === tag.id }"
              @click="filterByTag(tag.id)"
            >
              {{ tag.name }}
            </button>
          </div>
          <p v-else class="faint">暂无标签</p>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listBlogs, listCategories, listTags } from '../api'

const blogs = ref<any[]>([])
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const categoryId = ref<number>()
const tagId = ref<number>()

async function loadBlogs() {
  const res: any = await listBlogs({ pageNum: page.value, pageSize: 10, categoryId: categoryId.value, tagId: tagId.value })
  blogs.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function filterByCategory(id: number) { categoryId.value = categoryId.value === id ? undefined : id; tagId.value = undefined; page.value = 1; loadBlogs() }
function filterByTag(id: number) { tagId.value = tagId.value === id ? undefined : id; categoryId.value = undefined; page.value = 1; loadBlogs() }

onMounted(async () => {
  loadBlogs()
  try {
    const [catRes, tagRes]: any[] = await Promise.all([listCategories(), listTags()])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
  } catch { /* ignore */ }
})
</script>

<style scoped>
.count {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  padding: 5px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--surface);
}

.blog-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: var(--space-6);
  align-items: start;
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.blog-card {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-4);
  cursor: pointer;
  overflow: hidden;
}

.blog-thumb {
  width: 220px;
  aspect-ratio: 16 / 10;
  flex-shrink: 0;
  margin: 0;
}

.blog-body {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.blog-title {
  font-size: var(--text-lg);
  font-weight: 650;
  color: var(--color-text-strong);
}

.blog-desc {
  margin-top: 8px;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: var(--space-3);
  font-size: var(--text-xs);
  color: var(--color-text-faint);
}

.dot {
  color: var(--ink-300);
}

.blog-side {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  position: sticky;
  top: calc(var(--header-h) + var(--space-5));
}

.side-card {
  padding: var(--space-5);
}

.side-title {
  font-size: var(--text-base);
  font-weight: 650;
  margin-bottom: var(--space-3);
}

.cat-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cat-item {
  text-align: left;
  padding: 8px 10px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  font-size: var(--text-sm);
  font-family: inherit;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}
.cat-item:hover {
  background: var(--surface-muted);
  color: var(--color-text-strong);
}
.cat-item.active {
  background: var(--brand-50);
  color: var(--brand-600);
  font-weight: 600;
}

.tag-btn {
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tag-btn:hover {
  border-color: var(--brand-500);
}
.tag-btn.active {
  background: var(--brand-600);
  border-color: var(--brand-600);
  color: #fff;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--space-8);
}

@media (max-width: 980px) {
  .blog-layout {
    grid-template-columns: 1fr;
  }
  .blog-side {
    position: static;
  }
}

@media (max-width: 640px) {
  .blog-card {
    flex-direction: column;
    gap: var(--space-3);
  }
  .blog-thumb {
    width: 100%;
  }
}
</style>
