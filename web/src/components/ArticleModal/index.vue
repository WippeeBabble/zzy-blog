<template>
  <!-- <el-button text @click="dialogVisible = true"> click to open the Dialog </el-button> -->
  <el-dialog
    v-model="userStore.showArticleModal"
    title="文章管理"
    width="50%"
    :before-close="handleClose"
    :lock-scroll="false"
  >
    <!-- <span>This is a message</span> -->
    <!-- 子组件 -->

    <!-- 头部导航 -->
    <div class="header">
      <div
        :class="['btn_published', { active: btn_active == 'published' ? true : false }]"
        @click="showPublished"
      >
        已发表
      </div>
      <div
        :class="['btn_draft', { active: btn_active == 'draft' ? true : false }]"
        @click="showDraft"
      >
        草稿箱
      </div>
      <div
        :class="['btn_dustbin', { active: btn_active == 'dustbin' ? true : false }]"
        @click="showRecycleBin"
      >
        垃圾箱
      </div>
    </div>
    <hr class="line" />
    <!-- <div v-show="btn_active == 'published' ? true : false" >
      <Published  :publishedList="publishedList"/>
    </div> -->
    <div v-show="btn_active == 'draft' ? true : false">
      <Draft :draftData="draftData" @getDraftPage="getDraftPage" />
    </div>
    <!-- <div v-show="btn_active == 'dustbin' ? true : false" >
      <RecycleBin  :dustbinList="dustbinList"/>
    </div> -->
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="userStore.showArticleModal = false">Cancel</el-button>
        <el-button type="primary" @click="userStore.showArticleModal = false">
          Confirm
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch } from "vue"
// import Published from "./Published"
import Draft from "./Draft"
// import RecycleBin from "./RecycleBin"
import { getArticleHandleList } from "@/api"
import { useUserStore } from "@/store/user"
const userStore = useUserStore()
// import { ElMessageBox } from "element-plus"
// 关闭按钮
const handleClose = (done) => {
  userStore.showArticleModal = false
}
// 设置按钮active状态
let btn_active = ref("published")
// 设置事件更改btn——active
const showPublished = () => {
  btn_active.value = "published"
}
const showDraft = () => {
  btn_active.value = "draft"
}
const showRecycleBin = () => {
  btn_active.value = "dustbin"
}

//请求获取三种数据
let draftParams = ref({
  account: "test",
  pageNo: 1,
  status: "draft",
})
let draftData = ref(null)
// 获取新的页码
const getDraftPage = (val) => {
  // console.log(123,val)
  // 更改页码
  draftParams.value.pageNo = val
}
watch(
  draftParams,
  async (val) => {
    console.log(val)
    let result = await getArticleHandleList(val)
    console.log(result)
    if (result.code == 200) draftData.value = result.data
  },
  { immediate: true, deep: true }
)

// onMounted(async () => {
//   // 获取已发布文章数据
//   params.status = "published"
//   let publishedResult = await reqArticles(params)
//   if (publishedResult.code == 200) publishedList = publishedResult.data
//   else console.log("error")
//   // console.log(publishedList)
//   params.status = "draft"
//   let draftResult = await reqArticles(params)
//   if (draftResult.code == 200) draftData = draftResult.data
//   else console.log("error")

//   params.status = "deleted"
//   let dustbinResult = await reqArticles(params)
//   if (dustbinResult.code == 200) dustbinList = dustbinResult.data
//   else console.log("error")

//   console.log("已发布", publishedList)
//   console.log("草稿", draftData)
//   console.log("已删除", dustbinList)
//   // 获取草稿箱数据
// })

function reqArticles(params) {
  let result = getArticleHandleList(params)
  return result
}
</script>
<style scoped>
.dialog-footer button:first-child {
  margin-right: 10px;
}
.header {
  display: flex;
  width: 280px;
  justify-content: space-between;
  padding-left: 20px;
  margin-bottom: 10px;
}
.line {
  border: 1px solid rgba(0, 0, 0, 0.3);
}
/* 导航按钮 */
.btn_published,
.btn_draft,
.btn_dustbin {
  position: relative;
  width: 80px;
  font-size: 16px;
  height: 30px;
  line-height: 30px;
  text-align: center;
}
.btn_published::after,
.btn_draft::after,
.btn_dustbin::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  height: 2px;
  width: 0;
  border-radius: 1px;
  background-color: rgb(3, 145, 167);
  transition: all 0.2s;
}
.btn_published:hover::after,
.btn_draft:hover:after,
.btn_dustbin:hover:after {
  width: 100%;
}
.active {
  border-radius: 5px;
  background-color: #eee;
}
</style>
