<template>
  <div class="base">
    <!-- 注册登录界面 -->
    <div class="loginAndRegist">
      <!--登录表单-->
      <div class="loginArea">
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!-- 标语 -->
          <div v-show="isShow" class="title">
            LOGIN
          </div>
        </transition>
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!--登录框-->
          <div v-show="isShow" class="pwdArea">
            <div style="flex: 1;justify-content: center;align-items: center">
              <el-form :model="formData" label-width="auto" label-position="top"
                       style="margin-left: 22%;margin-right:30% ">
                <el-form-item label="用户名" style="margin-bottom: 0;margin-top: 6%">
                  <el-input v-model="formData.username" placeholder="Input your UserID" clearable></el-input>
                </el-form-item>
                <el-form-item label="密码" style="margin-bottom: 0">
                  <el-input v-model="formData.password" placeholder="Input your password" show-password type="password"
                            clearable></el-input>
                </el-form-item>


              </el-form>
            </div>

          </div>
        </transition>
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!-- 登录按钮 -->
          <div v-show="isShow" class="btnArea">
            <el-button type="success" round
                       style="width: 100px;margin-left: 25%;background-color:rgba(97,116,131,0.22);border: 1px solid #ccc8c8;letter-spacing: 2px"
                       @click="login">Login
            </el-button>
          </div>
        </transition>
      </div>


      <!-- 注册表单 -->
      <div class="registArea">
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!--  注册表头-->
          <div v-show="!isShow" class="rigestTitle" >
            REGISTER
          </div>
        </transition>
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!--            注册表单-->
          <div v-show="!isShow" class="registForm">
            <el-form :model="ruleForm" :rules="rules" ref="checkForm" status-icon  label-width="auto" >
              <el-form-item label="UserID" style="margin-bottom: 17px;margin-top: 3%" prop="username">
                <el-input v-model="ruleForm.username" placeholder="Input your UserID" clearable></el-input>
              </el-form-item>
              <el-form-item   label="Password" style="margin-bottom: 10px;margin-top: 0" prop="password">
                <el-input show-password type="password" placeholder="Input your Password" v-model="ruleForm.password" autocomplete="off"></el-input>
              </el-form-item>
              <div class="demo-progress" style="margin-left:25%;height: 15px;margin-top: 13px">
                <el-progress striped striped-flow :duration="8"  style="width: 240px;"  :percentage="progressLength" :format="format" :color="getColor" />
              </div>
              <el-form-item label="Confirm Password" style="margin-bottom: 17px;margin-top: 3px" prop="checkPass">
                <el-input show-password type="password" placeholder="Input the same password" v-model="ruleForm.checkPass"
                          autocomplete="off" maxlength="12"></el-input>
              </el-form-item>
            </el-form>
          </div>
        </transition>
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!--            注册按钮-->
          <div v-show="!isShow" class="registBtn">
            <el-button type="success" round
                       style="width: 100px;margin-left:37%;background-color: rgba(97,116,131,0.22);border: 1px solid #ccc8c8;letter-spacing: 2px"
                       @click="register">Signup
            </el-button>
            <el-button type="success" round
                       style="background-color: rgba(97,116,131,0.22);width: 100px;border: 1px solid #ccc8c8;letter-spacing: 2px"
                       @click="reSetForm">Reset info
            </el-button>
          </div>
        </transition>
      </div>
      <!-- 信息展示界面 -->
      <div id="aaa" class="showInfo" :style="{
                borderTopRightRadius: styleObj.bordertoprightradius,
                borderBottomRightRadius: styleObj.borderbottomrightradius,
                borderTopLeftRadius: styleObj.bordertopleftradius,
                borderBottomLeftRadius: styleObj.borderbottomleftradius,
                right: styleObj.rightDis
            }" ref="showInfoView">

        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!-- 没有用户输入用户名或者找不到用户名的时候 -->
          <div v-show="isShow"
               style="display: flex;flex-direction: column;align-items: center;justify-content: center;width: 100%;height: 100%">
            <!-- 欢迎语 -->
            <div
                style="flex: 2;display: flex;align-items: center;font-size: 22px;color:  rgba(97, 116, 131, 0.97);font-weight: bold">
              ParkingHelper
            </div>
            <!-- 欢迎图片 -->
            <div style="flex: 2">
              <el-button type="success" style="background-color: rgba(97,116,131,0.66);border: 1px solid #ccc8c8;" round
                         @click="changeToRegiest">Haven't account yet? Click to signup
              </el-button>
            </div>
          </div>
        </transition>
        <!-- 用户输入用户名时展示头像以及姓名 -->
        <transition name="animate__animated animate__bounce" enter-active-class="animate__fadeInUp"
                    leave-active-class="animate__zoomOut" appear>
          <!-- 用户注册的时候展示信息 -->
          <div v-show="!isShow"
               style="display: flex;flex-direction: column;align-items: center;justify-content: center;width: 100%;height: 100%">
            <!-- 欢迎语 -->
            <div
                style="flex: 2;display: flex;align-items: center;font-size: 22px;color: #FFFFFF;font-weight: bold">
              欢迎注册
            </div>
            <!-- 欢迎图片 -->
            <div style="flex: 2">
              <el-button type="success" style="background-color:rgba(97,116,131,0.66);border: 1px solid #ccc8c8;" round
                         @click="changeToLogin">Have account already？Click to login
              </el-button>
            </div>
          </div>
        </transition>
      </div>
    </div>

  </div>
</template>

<script setup>
import {reactive, ref, watch} from "vue";
import api from "../../api/index.js";
import {ElMessage, ElNotification} from "element-plus";
import {useRouter} from "vue-router";
import {useStore} from "vuex";
const store=useStore();
const router = useRouter()
const formData = reactive({
  username: "",
  password: ""
})

const login = () => {
  //如果为管理员 =>header部分显示 成果信息后台管理系统
  api.post("/login", formData).then(res => {
    console.log("登陆之后后端返回的数据是 ", res.data);
    if (res.data.flag === true) {
      //登陆成功  计数 +1
      store.commit("inittabList")//登录成功之后 初始化面包屑 和tag标签
      //存入token
      let tokenValue = res.data.data.tokenValue;
      let tokenName = res.data.data.tokenName;
      let username  = res.data.data.loginId;
      //存入角色
      if (tokenValue && tokenName) {
        let role = res.data.message;
        sessionStorage.setItem('saToken', tokenValue); //会话存储里面
        sessionStorage.setItem('tokenName', tokenName);
        sessionStorage.setItem('role', role);
        sessionStorage.setItem('username', username);
        ElMessage({
          message: '登录成功,欢迎回来! ' + formData.username,
          type: 'success',
        })


          router.push("/")


      }
    } else {
      ElMessage({
        message: res.data.message,
        type: 'error',
      })
    }
  })
}

const styleObj = reactive({
  bordertoprightradius: '15px',
  borderbottomrightradius: '15px',
  bordertopleftradius: '0px',
  borderbottomleftradius: '0px',
  rightDis: '0px'
})
const isShow = ref(true);

const changeToRegiest = () => {
  styleObj.bordertoprightradius = '0px'
  styleObj.borderbottomrightradius = '0px'
  styleObj.bordertopleftradius = '15px'
  styleObj.borderbottomleftradius = '15px'
  styleObj.rightDis = '50%'
  isShow.value = !isShow.value
};
const changeToLogin = () => {
  styleObj.bordertoprightradius = '15px'
  styleObj.borderbottomrightradius = '15px'
  styleObj.bordertopleftradius = '0px'
  styleObj.borderbottomleftradius = '0px'
  styleObj.rightDis = '0px'
  isShow.value = !isShow.value
}

const checkForm = ref(null) //ref对象
//自定义校验规则  校验两次输入的密码\
//校验输入的密码


const rules = reactive({//校验规则
  username: [{required: true, message: '用户名为学号', trigger: 'blur'}, {
    //长度为6
    min: 3,
    max: 10,
    message: '用户名为学号 长度为6',
    trigger: 'blur'
  }],

  password :[{ required: true, message: '密码', trigger: 'blur' }
    ,{
    //长度为6
    min: 2,
    max: 10,
    message: '密码为',
    trigger: 'blur'
  }
  ],
  checkPass:[{required: true, message: '确认密码', trigger: 'blur' },{
    //长度为6
    min: 3,
    max: 10,
    message: '确认密码为',
    trigger: 'blur'
  }],

})
const ruleForm=reactive({
  username:"",
  password: '',
})
const progressLength=ref(0)
//进度条
//监听对象数据
watch(()=>ruleForm.password,(newValue,oldValue)=>{
  progressLength.value=newValue.length*10;
})
const format=(percentage)=> {
  if (percentage <=30) {
    return 'Password strength: Weak';
  } else if (percentage>30&&percentage<=60 ) {
    return 'Password strength: Medium';
  }
  else  if(percentage>60&&percentage<=80){
    return 'Password strength: Strong';
  }
  else{
    return"Password strength: Strong"
  }
}
const getColor=(percentage)=>{
  if (percentage <=30) {
    return '#f56c6c';
  } else if (percentage<=60 &&percentage>30) {
    return '#e6a23c';
  }
  else  if(percentage>60&&percentage<80){
    return '#1989fa';
  }
  else {
    return '#5cb87a';
  }

}

//用户注册
const register=()=>{
  console.log(ruleForm.area);
  //比较两个密码
  if(ruleForm.password!==ruleForm.checkPass){
    ElMessage({
      type: 'warning',
      message: 'Passwords do not match（',
    })
    return;
  }
  checkForm.value.validate((valid) => {
    if(valid){
      api.post("/register" , ruleForm).then(res => {
        console.log("编辑之后的 res is ",res);
        if (res.data.flag === true) {
          ElMessage({
            type: 'success',
            message: res.data.message,
          })
        } else {
          ElMessage({
            type: 'warning',
            message: res.data.message,
          })
        }
      })
    }
    else{
      ElMessage({
        type: 'warning',
        message: 'Please check if the form fields are valid',
      })
    }
  })
}
const reSetForm=()=>{
  ruleForm.username="";
  ruleForm.password= ''
  ruleForm.role= "学生"
  ruleForm.name=""
  ruleForm.major=''
  ruleForm.checkPass= ''
  ruleForm.status=1
  ruleForm.area=""
}
//找回密码
const findPwd=()=>{
  router.push({name:'passwordRecovery'})
  //跳转到找回密码的页面
}
</script>

<style scoped>
.base {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("../../assets/login/bgimg.png");

  background-size: 100% 100%;
}

.loginAndRegist {
  border: #e9fcff solid ;
  border-radius: 50px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.loginArea {
  /*background-color: rgba(239, 233, 233, 0.78);*/
  border-top-left-radius: 15px;
  border-bottom-left-radius: 15px;
  height: 430px;
  width: 540px;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
}

.registArea {
  border-top-right-radius: 15px;
  border-bottom-right-radius: 15px;
  height: 430px;
  width: 540px;
  /*background-color: rgba(239, 233, 233, 0.8);*/
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.showInfo {
  border-top-right-radius: 15px;
  border-bottom-right-radius: 15px;
  position: absolute;
  z-index: 2;
  height: 430px;
  width: 540px;
  display: flex;
  justify-content: center;
  align-items: center;
  /*background-image: url("../../assets/images/inside.jpg");*/
  background-size: 80% 85%;
}

.showInfo:hover {
  background-size: 100%;
  background-position: -80px -70px;
}
.title {
  width: 70%;
  flex: 1;
  border-bottom: 1px solid rgba(97, 116, 131, 0.97);
  display: flex;
  align-items: center;
  color: rgba(97, 116, 131, 0.97);
  font-weight: bold;
  font-size: 24px;
  display: flex;
  justify-content: center;
}

#aaa {
  transition: 0.3s linear;
}

.pwdArea {
  width: 100%;
  flex: 2;
  display: flex;
  flex-direction: column;
  display: flex;
  flex-direction: column;


}

.pwdArea input {
  outline: none;
  height: 30%;
  border-radius: 13px;
  padding-left: 10px;
  font-size: 12px;
  border: 1px solid gray;
}

.pwdArea input:focus {
  border: 2px solid rgba(97, 116, 131, 0.97);
}

.btnArea {
  display: flex;
  flex: 1;
  width: 100%;
  display: flex;
  /*justify-content: space-around; !*水平居中*!*/
}

.rigestTitle {
  margin-left: 10%;
  width: 70%;
  flex: 1;
  color: rgba(97, 116, 131, 0.97);
  font-weight: bold;
  font-size: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid rgba(97, 116, 131, 0.97);
}

.registForm {
  flex: 2;
  width: 60%;
  flex-direction: column;
  color: rgba(97, 116, 131, 0.97);
  font-size: 16px;
}
.registBtn {
  width: 100%;
  flex: 1;
  display: flex;
  margin-top: 1px;
}
</style>
