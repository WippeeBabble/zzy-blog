<template>
  <Background :title="`Category`"></Background>
  <div class="container basic-box" >
    <div class="articleTitle">
      <Tree :data="data"></Tree>
    </div>
  </div>
  <Waifu/>
</template>

<script setup>
import { ref, reactive, computed , onMounted } from "vue"
import { useArticleStore } from "@/store/article"
import Tree from "./Tree"
const articleStore = useArticleStore()

let data = computed(() => {
  return addShowFuc(articleStore.groupList)
})

// 看一看d传过来的data
// onMounted(() => console.log(data))

// // 写一个addshow方法 把所有数据遍历一遍，都加上show属性
const addShowFuc = (data) => {
  // return data.map((item) => {
  //   item.show = false
  //   if (item.children) {
  //     addShowFuc(item.children)
  //   }

  //   return item
  // }

  return data.map((item) => {
    if(item.children){
      item.show = false
      addShowFuc(item.children)
    }
    return item
  }
  )
}
//#region
// 做一个映射 id:item
// foreach 成功了 reduce不知道为甚不行
// AllItem.reduce((memo,current)=>{
// },{})

// let Alldata = {}
// AllItem.forEach(element => {
//     Alldata[element["id"]] = element;
// });
// let Lastlist = []
// AllItem.forEach((item) =>{
//     let pid = item.pid
//     let parant = Alldata[pid]
//     if (pid == 0){
//       Lastlist.push(item)
//     } else if(parant){
//       parant.children? parant.push(item) : parant.children = [item]
//     }
// })
//#endregion
</script>

<style scoped>
.container {
  width: 1200px;
  min-height: 400px;
  background-color: #fff;

  margin: 0 auto;
  padding: 1px;
  transform: translateY(-50px);

  
}
.articleTitle {
  margin: 48px 80px;
}
</style>
