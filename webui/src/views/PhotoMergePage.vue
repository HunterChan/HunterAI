<template>
  <div class="photo-merge-container">
    <el-row :gutter="10">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="compact-header">
            <h2>甜影合拍</h2>
            <span class="subtitle">上传两张素材，AI智能合成甜蜜合拍照</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mt-20">
      <!-- 左侧操作面板 -->
      <el-col :span="6">
        <el-card class="control-panel">
          <template #header>
            <div class="panel-header">
              <h3>上传素材</h3>
            </div>
          </template>
          <div class="upload-area">
            <el-row :gutter="10">
              <el-col :span="24">
                <el-upload
                  class="upload-box"
                  drag
                  action="#"
                  :auto-upload="false"
                  :limit="1"
                  :on-change="handleFile1Change"
                  :on-remove="handleFile1Remove"
                  :file-list="fileList1"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
                >
                  <template v-if="previewUrl1">
                    <div class="image-preview">
                      <img :src="previewUrl1" class="preview-img" />
                      <div class="preview-actions">
                        <el-button type="danger" size="small" circle @click.stop="handleFile1Remove(fileList1[0])">
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                    <div class="el-upload__text">
                      点击或拖入素材
                      <div class="upload-tip">推荐尺寸 1920×1080<br>支持PNG/JPG/MP4</div>
                    </div>
                  </template>
                </el-upload>
              </el-col>
            </el-row>
          </div>

          <div class="control-section mt-20">
            
            <div class="scene-selection">
              <h4>场景模板</h4>
              <el-scrollbar height="110px">
                <div class="scene-grid">
                  <div 
                    v-for="scene in scenes" 
                    :key="scene.id" 
                    class="scene-item" 
                    :class="{ 'selected': mergeForm.sceneId === scene.id }"
                    @click="selectScene(scene)"
                  >
                    <el-tooltip 
                      :content="scene.description || '暂无描述'" 
                      placement="top"
                      effect="light"
                      :show-after="300"
                    >
                      <div class="scene-wrapper">
                        <el-image 
                          :src="getImageUrl(scene.previewImage)" 
                          fit="contain"
                          class="scene-preview"
                        ></el-image>
                        <div class="scene-name">{{ scene.name }}</div>
                      </div>
                    </el-tooltip>
                  </div>
                </div>
              </el-scrollbar>
            </div>
            
            <div class="prompt-section mt-20">
              <h4>提示词</h4>
              <el-input 
                v-model="mergeForm.customPrompt" 
                type="textarea" 
                :rows="4"
                placeholder="输入提示词，描述你希望生成的效果..."
              ></el-input>
              <div class="prompt-actions">
                <el-tooltip content="使用DeepSeek-R1生成专业提示词" placement="top">
                  <el-button type="primary" text @click="optimizePrompt" :loading="loading.optimizing">
                    <el-icon><magic-stick /></el-icon> 优化提示词
                  </el-button>
                </el-tooltip>
                <el-select v-model="selectedPromptId" placeholder="选择预设提示词" @change="useSelectedPrompt" class="prompt-select">
                  <el-option
                    v-for="prompt in prompts"
                    :key="prompt.id"
                    :label="prompt.content.substring(0, 20) + '...'"
                    :value="prompt.id">
                  </el-option>
                </el-select>
              </div>
            </div>
          </div>
          
          <div class="action-section mt-20">
            <el-button type="primary" size="large" @click="createMerge" :loading="loading.submit" :disabled="!canSubmit">
              开始智能合成
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 中间预览面板 -->
      <el-col :span="14">
        <el-card class="preview-panel">
          <template #header>
            <div class="panel-header">
              <h3>实时预览</h3>
            </div>
          </template>
          <div class="preview-area" v-loading="loading.preview">
            <div v-if="!previewImage && !processingMerge" class="preview-placeholder">
              <el-empty description="上传素材并点击开始智能合成查看效果"></el-empty>
            </div>
            <div v-else-if="processingMerge" class="processing-preview">
              <el-progress type="circle" :percentage="processingProgress" :status="processingStatus"></el-progress>
              <p class="processing-text">{{processingText}}</p>
            </div>
            <div v-else class="result-preview">
              <div class="preview-image-container">
                <el-image 
                  :src="previewImage" 
                  fit="scale-down"
                  class="preview-image"
                  :preview-src-list="[previewImage]"
                  :initial-index="0"
                ></el-image>
              </div>
              <div class="preview-actions">
                <el-button type="primary" @click="downloadPreview">
                  <el-icon><download /></el-icon> 下载
                </el-button>
                <el-button type="info" @click="regenerateMerge">
                  <el-icon><refresh-right /></el-icon> 重新生成
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧历史记录 -->
      <el-col :span="4">
        <el-card class="history-panel">
          <template #header>
            <div class="panel-header">
              <h3>历史记录</h3>
              <el-button type="text" @click="navigateTo('photomerge/history')">查看全部</el-button>
            </div>
          </template>
          <div class="recent-merges" v-loading="loading.recentMerges">
            <el-scrollbar height="150px">
              <el-empty v-if="recentMerges.length === 0" description="暂无合成记录"></el-empty>
              <div v-else class="history-grid">
                <div v-for="merge in recentMerges" :key="merge.id" class="history-item" @click="loadHistoryItem(merge)">
                  <el-image 
                    :src="getImageUrl(merge.resultPath)" 
                    fit="contain"
                    v-if="merge.status === 'COMPLETED'">
                  </el-image>
                  <div v-else class="pending-image">
                    <el-icon><loading /></el-icon>
                  </div>
                  <div class="history-info">
                    <span class="history-date">{{ formatDate(merge.createdAt) }}</span>
                    <el-tag :type="getStatusType(merge.status)" size="small">{{ getStatusText(merge.status) }}</el-tag>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, 
  Delete, 
  RefreshRight, 
  UploadFilled, 
  Loading, 
  Picture,
  MagicStick
} from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

// 文件上传列表和预览URL
const fileList1 = ref([])
const previewUrl1 = ref('')

// 预览状态
const previewImage = ref('')
const processingMerge = ref(false)
const processingProgress = ref(0)
const processingStatus = ref('')
const processingText = ref('AI正在合成中...')
const lastMergeId = ref(null)

// 表单数据
const mergeForm = reactive({
  photo1: null,
  sceneId: '',
  customPrompt: ''
})

// 加载状态
const loading = reactive({
  recentMerges: false,
  scenes: false,
  preview: false,
  submit: false,
  optimizing: false
})

// 数据列表
const recentMerges = ref([])
const scenes = ref([])
const prompts = ref([])
const selectedPromptId = ref('')

// 模拟场景数据（当API加载失败时使用）
const mockScenes = [
  {
    id: 1,
    name: '浪漫海滩',
    description: '在美丽的海滩上漫步，享受阳光和海浪',
    previewImage: '/images/scenes/beach.jpg',
    isActive: true
  },
  {
    id: 2,
    name: '城市夜景',
    description: '在繁华的城市夜景中留下美好回忆',
    previewImage: '/images/scenes/city_night.jpg',
    isActive: true
  },
  {
    id: 3,
    name: '樱花庄园',
    description: '在樱花盛开的庄园中感受浪漫氛围',
    previewImage: '/images/scenes/cherry_blossom.jpg',
    isActive: true
  },
  {
    id: 4,
    name: '童话森林',
    description: '如同童话故事里的神秘森林，充满魔幻色彩',
    previewImage: '/images/scenes/fairy_forest.jpg',
    isActive: true
  },
  {
    id: 5,
    name: '复古风格',
    description: '怀旧复古风格的背景，仿佛回到过去的年代',
    previewImage: '/images/scenes/vintage.jpg',
    isActive: true
  }
]

// 计算属性：是否可以提交
const canSubmit = computed(() => {
  return mergeForm.photo1 && mergeForm.sceneId
})

// 上传文件变化回调
const handleFile1Change = (uploadFile, uploadFiles) => {
  // 确保是选择文件的操作而不是删除操作
  if (uploadFiles.length > 0) {
    const file = uploadFile
    
    // 先清理之前的URL（如果存在）
    if (previewUrl1.value && previewUrl1.value.startsWith('blob:')) {
      URL.revokeObjectURL(previewUrl1.value)
    }
    
    mergeForm.photo1 = file.raw
    
    // 仅保留最新的文件
    fileList1.value = [file]
    
    // 创建本地预览
    if (file.raw instanceof File && file.raw.type.startsWith('image/')) {
      const url = URL.createObjectURL(file.raw)
      file.url = url
      previewUrl1.value = url
      console.log('预览URL1已创建:', previewUrl1.value)
    }
        ElMessage.success('素材已选择')
  }
}

// 文件移除回调
const handleFile1Remove = (file) => {
  if (file && file.url && file.url.startsWith('blob:')) {
    URL.revokeObjectURL(file.url)
  }
  fileList1.value = []
  mergeForm.photo1 = null
  previewUrl1.value = ''
}

// 选择场景
const selectScene = (scene) => {
  mergeForm.sceneId = scene.id
}

// 优化提示词
const optimizePrompt = async () => {
  if (!mergeForm.customPrompt) {
    ElMessage.warning('请先输入需要优化的提示词')
    return
  }
  
  loading.optimizing = true
  try {
    const response = await axios.post('/api/prompts/optimize', {
      prompt: mergeForm.customPrompt
    })
    
    if (response.data.success) {
      mergeForm.customPrompt = response.data.optimizedPrompt
      ElMessage.success('提示词优化成功')
    } else {
      ElMessage.error(response.data.message || '优化失败')
    }
  } catch (error) {
    ElMessage.error('提示词优化服务暂不可用')
    console.error('Failed to optimize prompt:', error)
  } finally {
    loading.optimizing = false
  }
}

// 使用选择的预设提示词
const useSelectedPrompt = () => {
  if (!selectedPromptId.value) return
  
  const selectedPrompt = prompts.value.find(p => p.id === selectedPromptId.value)
  if (selectedPrompt) {
    mergeForm.customPrompt = selectedPrompt.content
  }
}

// 创建合成
const createMerge = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请上传素材并选择场景')
    return
  }
  
  loading.submit = true
  processingMerge.value = true
  processingProgress.value = 0
  processingText.value = 'AI正在合成中...'
  previewImage.value = ''
  
  try {
    // 创建FormData对象用于上传文件
    const formData = new FormData()
    formData.append('photo1', mergeForm.photo1)
    formData.append('sceneId', mergeForm.sceneId)
    formData.append('promptText', mergeForm.customPrompt || '')
    
    const response = await axios.post('/api/merges', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.data.success) {
      lastMergeId.value = response.data.mergeId
      startProgressSimulation()
      ElMessage.success('合成请求已提交')
      
      // 3秒后模拟获取结果（实际项目中应通过轮询或WebSocket获取实时进度）
      setTimeout(() => {
        fetchMergeResult(response.data.mergeId)
      }, 3000)
    } else {
      processingMerge.value = false
      ElMessage.error(response.data.message || '提交失败')
    }
  } catch (error) {
    processingMerge.value = false
    ElMessage.error(error.response?.data?.message || '提交失败')
    console.error('Failed to create merge:', error)
  } finally {
    loading.submit = false
  }
}

// 模拟进度更新
const startProgressSimulation = () => {
  processingProgress.value = 0
  let intervalId = setInterval(() => {
    if (processingProgress.value < 90) {
      processingProgress.value += Math.floor(Math.random() * 10) + 1
      
      // 根据进度更新文本
      if (processingProgress.value < 30) {
        processingText.value = "AI正在分析素材..."
      } else if (processingProgress.value < 60) {
        processingText.value = "AI正在构思创意合成效果..."
      } else {
        processingText.value = "AI正在渲染最终图像..."
      }
    } else {
      clearInterval(intervalId)
    }
  }, 500)
}

// 获取合成结果
const fetchMergeResult = async (mergeId) => {
  loading.preview = true
  try {
    const response = await axios.get(`/api/merges/${mergeId}`)
    
    if (response.data.status === 'COMPLETED') {
      processingProgress.value = 100
      processingStatus.value = 'success'
      processingText.value = "合成完成！"
      previewImage.value = getImageUrl(response.data.resultPath)
      
      // 延迟关闭进度条
      setTimeout(() => {
        processingMerge.value = false
      }, 1000)
      
      // 刷新最近合成列表
      fetchRecentMerges()
    } else if (response.data.status === 'FAILED') {
      processingProgress.value = 100
      processingStatus.value = 'exception'
      processingText.value = "合成失败，请重试"
      setTimeout(() => {
        processingMerge.value = false
      }, 1000)
    } else {
      // 如果仍在处理中，继续轮询
      setTimeout(() => {
        fetchMergeResult(mergeId)
      }, 2000)
    }
  } catch (error) {
    processingMerge.value = false
    processingStatus.value = 'exception'
    ElMessage.error('获取合成结果失败')
    console.error('Failed to fetch merge result:', error)
  } finally {
    loading.preview = false
  }
}

// 下载预览图
const downloadPreview = () => {
  if (!previewImage.value || !lastMergeId.value) return
  window.location.href = `/api/merges/${lastMergeId.value}/download`
}

// 重新生成
const regenerateMerge = async () => {
  if (!lastMergeId.value) return
  
  processingMerge.value = true
  processingProgress.value = 0
  processingText.value = 'AI正在重新合成...'
  previewImage.value = ''
  
  try {
    const response = await axios.post(`/api/merges/${lastMergeId.value}/regenerate`)
    
    if (response.data.success) {
      startProgressSimulation()
      
      // 3秒后模拟获取结果
      setTimeout(() => {
        fetchMergeResult(lastMergeId.value)
      }, 3000)
    } else {
      processingMerge.value = false
      ElMessage.error(response.data.message || '重新生成失败')
    }
  } catch (error) {
    processingMerge.value = false
    ElMessage.error('重新生成失败')
    console.error('Failed to regenerate:', error)
  }
}

// 加载历史记录项
const loadHistoryItem = (merge) => {
  if (merge.status !== 'COMPLETED') return
  
  lastMergeId.value = merge.id
  previewImage.value = getImageUrl(merge.resultPath)
  processingMerge.value = false
}

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path)
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'COMPLETED': return 'success'
    case 'PROCESSING': return 'info'
    case 'FAILED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'COMPLETED': return '已完成'
    case 'PROCESSING': return '处理中'
    case 'FAILED': return '失败'
    default: return '未知'
  }
}

// 获取图片URL
const getImageUrl = (path) => {
  console.log('尝试获取图片:', path);
  
  // 如果是前端本地图片路径
  if (path && path.startsWith('/')) {
    const localPath = path;
    console.log('识别为本地图片路径:', localPath);
    
    // 添加错误处理回调，捕获图片加载错误
    setTimeout(() => {
      const imgElements = document.querySelectorAll(`img[src="${localPath}"]`);
      imgElements.forEach(img => {
        if (!img.hasAttribute('data-error-handler')) {
          img.setAttribute('data-error-handler', 'true');
          img.addEventListener('error', () => {
            console.error('本地图片加载失败:', localPath);
            // 尝试使用备用路径
            if (!img.src.includes('placeholder.png')) {
              console.log('切换到占位图', img.src);
              img.src = '/images/placeholder.png';
            }
          });
        }
      });
    }, 100);
    
    return localPath;
  }
  
  // 如果路径为空或无效，返回占位图
  if (!path) {
    console.log('路径为空，使用占位图');
    return '/images/placeholder.png';
  }
  
  // 如果是服务器路径
  console.log('识别为服务器路径:', path);
  
  // 使用后端API获取图片
  let apiPath;
  if (path.startsWith('./uploads/')) {
    // 移除./uploads/前缀，适配API路径
    apiPath = path.substring('./uploads/'.length);
    console.log('处理上传路径为:', apiPath);
  } else if (path.startsWith('.\\uploads\\results\\')) {
    // 获取路径最后一个反斜杠后的文件名
    apiPath = 'results/' + path.substring(path.lastIndexOf('\\') + 1);
    console.log('提取结果图片文件名:', apiPath);
  } else if (path.startsWith('./')) {
    apiPath = path.substring(2);
    console.log('处理相对路径为:', apiPath);
  } else {
    apiPath = path;
    console.log('使用原始路径:', apiPath);
  }
  
  const finalUrl = `/api/files/images/${apiPath}`;
  console.log('最终图片URL:', finalUrl);
  return finalUrl;
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 获取场景列表
const fetchScenes = async () => {
  loading.scenes = true
  console.log('开始请求场景数据...')
  
  // 直接使用模拟数据，确保UI正常显示
  //scenes.value = mockScenes
  //console.log('使用模拟数据:', JSON.stringify(scenes.value))
  
  try {
    // 记录完整请求信息
    console.log('请求URL:', '/api/scenes')
    console.log('请求头:', {
      'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
      'Content-Type': 'application/json'
    })
    
    // 添加认证头和更详细的错误处理
    const response = await axios.get('/api/scenes', {
      headers: {
        'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
        'Content-Type': 'application/json'
      }
    })
    
    console.log('场景API响应:', response)
    
    // 检查响应状态和数据
    if (response.status !== 200) {
      throw new Error(`API返回状态码: ${response.status}`)
    }
    
    if (!response.data || !Array.isArray(response.data)) {
      console.warn('API返回数据格式不正确:', response.data)
      return // 已经使用了模拟数据，不需要再次赋值
    }
    
    if (response.data.length > 0) {
      // 临时保存API返回的场景数据，但不覆盖当前模拟数据
      const apiScenes = response.data
      console.log('API返回的场景数据:', JSON.stringify(apiScenes))
      
      // 检查并修复图片路径
      apiScenes.forEach(scene => {
        if (scene.previewImage) {
          console.log(`场景[${scene.name}]原始图片路径: ${scene.previewImage}`)
        } else {
          console.warn(`场景[${scene.name}]缺少图片路径`)
        }
      })
      
      // 暂时注释掉，先不使用API数据，使用模拟数据
      scenes.value = apiScenes
      console.log('成功从API加载场景数据:', scenes.value)
    } else {
      console.warn('警告: API返回的场景列表为空，继续使用模拟数据')
    }
  } catch (error) {
    console.error('加载场景列表失败:', error)
    console.error('错误详情:', {
      message: error.message,
      name: error.name,
      stack: error.stack,
      response: error.response ? {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data
      } : null,
      request: error.request ? '请求已发送但无响应' : null
    })
    ElMessage.error('加载场景列表失败，使用模拟数据')
    // 已经使用了模拟数据，不需要再次赋值
  } finally {
    loading.scenes = false
  }
}

// 获取最近合成记录
const fetchRecentMerges = async () => {
  loading.recentMerges = true
  try {
    const response = await axios.get('/api/merges?limit=6')
    recentMerges.value = response.data
  } catch (error) {
    console.error('Failed to load recent merges:', error)
    ElMessage.error('加载最近合成记录失败')
  } finally {
    loading.recentMerges = false
  }
}

// 获取提示词列表
const fetchPrompts = async () => {
  try {
    const response = await axios.get('/api/prompts')
    prompts.value = response.data
  } catch (error) {
    console.error('Failed to load prompts:', error)
  }
}

// 上传前钩子
const beforeUpload = (file) => {
  // 这里可以添加文件类型检查等逻辑
  // 返回false阻止自动上传
  return false
}

onMounted(async () => {
  fetchScenes()
  fetchRecentMerges()
  fetchPrompts()
})

// 组件卸载前释放创建的对象URL，避免内存泄漏
onBeforeUnmount(() => {
  // 清理预览URL
  if (previewUrl1.value && previewUrl1.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewUrl1.value)
  }
  
  fileList1.value.forEach(file => {
    if (file.url && file.url.startsWith('blob:')) {
      URL.revokeObjectURL(file.url)
    }
  })
})
</script>

<style scoped>
.photo-merge-container {
  padding: 10px;
}

.mt-20 {
  margin-top: 10px;
}

.mt-10 {
  margin-top: 10px;
}

.welcome-card {
  background-color: #fff;
}

.compact-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0px 20px;
}

.compact-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.upload-area {
  margin-bottom: 50px;
}

.upload-box {
  height: 90px;
  width: 100%;
  position: relative;
}

.upload-box .el-upload-dragger {
  width: 100%;
  height: 90px;
}

.image-preview {
  width: 100%;
  height: 90px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background-color: #f5f7fa;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-actions {
  position: absolute;
  top: 5px;
  right: 5px;
  display: flex;
  gap: 5px;
  z-index: 10;
}

.image-preview:hover::before {
  content: '点击或拖入新图片替换';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  z-index: 5;
  cursor: pointer;
}

.upload-tip {
  font-size: 10px;
  color: #909399;
  margin-top: 4px;
}

.el-upload__text {
  font-size: 12px;
}

.el-icon--upload {
  font-size: 20px;
  margin-bottom: 4px;
}

.control-section {
  margin-top: 20px;
}

.control-section h4 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #606266;
  font-weight: bold;
}

.scene-selection {
  margin-top: 15px;
}

.scene-grid {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  padding: 5px;
}

.scene-item {
  width: 110px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
  position: relative;
}

.scene-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scene-item.selected {
  border-color: #409EFF;
  transform: translateY(-3px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.scene-preview {
  width: 100%;
  height: 100px;
  display: block;
  object-fit: contain;
  background-color: #f0f2f5;
}

.scene-name {
  font-size: 12px;
  padding: 6px 4px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background-color: #fff;
}

.prompt-section {
  margin-top: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.prompt-section .el-textarea {
  margin-top: 10px;
}

.prompt-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.prompt-select {
  width: 150px;
}

.action-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 15px 0;
}

.action-section .el-button {
  width: 80%;
  height: 45px;
  font-size: 16px;
}

.preview-panel {
  height: calc(100vh - 240px);
  display: flex;
  flex-direction: column;
}

.preview-panel .el-card__body {
  flex: 1;
  overflow: hidden;
}

.recent-merges {
  height: 100%;
}

.history-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px;
}

.history-item {
  width: 100%;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.history-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.history-item .el-image {
  width: 100%;
  height: 160px;
  display: block;
  object-fit: cover;
}

.pending-image {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.pending-image .el-icon {
  font-size: 24px;
  color: #909399;
}

.history-info {
  padding: 8px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.history-date {
  font-size: 12px;
  color: #909399;
}

.history-panel {
  height: calc(100vh - 240px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.history-panel .el-card__body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.control-panel {
  height: calc(100vh - 240px);
  display: flex;
  flex-direction: column;
}

.control-panel .el-card__body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 20px;
}
</style> 