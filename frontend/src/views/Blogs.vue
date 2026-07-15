<template>
  <div class="page-container">
    <h1 style="margin-bottom:20px">博客</h1>
    <el-row :gutter="20">
      <el-col :span="18">
        <el-card v-for="blog in blogs" :key="blog.id" shadow="hover" class="card-hover" style="margin-bottom:16px;cursor:pointer" @click="$router.push('/blogs/' + blog.id)">
          <div style="display:flex;gap:16px">
            <el-image v-if="blog.cover" :src="blog.cover" style="width:200px;height:120px;flex-shrink:0" fit="cover" />
            <div>
              <h2 style="margin-bottom:8px">{{ blog.title }}</h2>
              <p style="color:#999;font-size:14px">{{ blog.summary }}</p>
              <div style="margin-top:8px;color:#bbb;font-size:12px">{{ blog.publishTime }} · {{ blog.viewCount }} 次阅读</div>
            </div>
          </div>
        </el-card>
        <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadBlogs" style="margin-top:20px;text-align:center" />
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header><h3>分类</h3></template>
          <div v-for="cat in categories" :key="cat.id" style="margin-bottom:8px;cursor:pointer" @click="filterByCategory(cat.id)">
            {{ cat.name }}
          </div>
        </el-card>
        <el-card style="margin-top:16px">
          <template #header><h3>标签</h3></template>
          <el-tag v-for="tag in tags" :key="tag.id" style="margin:4px;cursor:pointer" @click="filterByTag(tag.id)">{{ tag.name }}</el-tag>
        </el-card>
      </el-col>
    </el-row>
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

function filterByCategory(id: number) { categoryId.value = id; tagId.value = undefined; page.value = 1; loadBlogs() }
function filterByTag(id: number) { tagId.value = id; categoryId.value = undefined; page.value = 1; loadBlogs() }

onMounted(async () => {
  loadBlogs()
  try {
    const [catRes, tagRes]: any[] = await Promise.all([listCategories(), listTags()])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
  } catch { /* ignore */ }
})
</script>
