<template>
    <el-row type="flex" class="row-bg" justify="center">

        <el-col old="{ span: 10, offset: 1 }" l8p="{ span: 10, offset: 1 }" style="float:left;position: relative;left:10%">
            <h2>欢迎使用VueManager管理系统</h2>
            <el-image :src="require('@/assets/logo.png')" style="width: 320px;height: 240px"></el-image>
            <h3>管理至简，运维无忧</h3>
        </el-col>

        <el-col old="{ span: 1, offset: 1 }" l8p="{ span: 1, offset: 1 }">
            <el-divider direction="vertical"></el-divider>
        </el-col>

        <el-col old="{ span: 10, offset: 1 }" l8p="{ span: 10, offset: 1 }" style="float:right;position: relative;right:5%">
            <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="100px" class="demo-loginForm">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="loginForm.username" style="width:300px;float:left;"></el-input>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                    <el-input v-model="loginForm.password" type="password" style="width:300px;float:left;"></el-input>
                </el-form-item>

                <el-form-item label="验证码" prop="code">
                    <el-input v-model="loginForm.code" style="width:170px;float:left;"></el-input>
                    <el-image :src="codeImg" class="codeImg" @click="getCaptcha()"></el-image>
                </el-form-item>
                <el-form-item >
                    <el-button type="primary" @click="submitForm('loginForm')">登录</el-button>
                    <el-button @click="resetForm('loginForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </el-col>
    </el-row>
</template>

<script>

    import qs from 'qs'

    export default {
        name:"Login",
        data() {
            return {
                loginForm: {
                    username: '',
                    password: '',
                    code: '',
                    token:''
                },
                rules: {
                    username: [
                        { required: true, message: '请输入用户名', trigger: 'blur' }

                    ],
                    password: [
                        { required: true, message: '请输入密码', trigger: 'blur' }
                    ],
                    code: [
                        { required: true, message: '请输入验证码', trigger: 'change' },
                        {min:5,max:5,message:  '长度为5个字符' ,trigger: 'blur'}
                    ]
                },
                codeImg:''
            };
        },
        methods: {
            submitForm(formName) {
                this.$refs[formName].validate((valid) => {
                    if (valid) {
                        this.$axios.post('/login?'+ qs.stringify(this.loginForm)).then(res => {

                            const jwt = res.headers['authorization']
                            this.$store.commit('SET_TOKEN',jwt)
                            this.$router.push("/index")

                        })
                        .catch(error => {

                            console.log('登录失败：', error)
                            this.getCaptcha()
                        })

                    }
                    else {
                        console.log('验证失败');
                        return false;
                    }
                });
            },
            resetForm(formName) {
                this.$refs[formName].resetFields();
            },
            getCaptcha() {
                this.$axios.get('/captcha').then(res =>{
                    if(res.data && res.data.data){
                        this.loginForm.token = res.data.data.token || ''
                        console.log("moke (模拟服务器生成的随机码: )",this.loginForm.token)
                        this.codeImg = res.data.data.captchaImg  || ''
                        this.loginForm.code =''
                    }
                })
                .catch(error  =>{
                    console.log('获取验证码失败:' , error)
                })
            }
        },
        created(){
            this.getCaptcha()
        }
    }
</script>

<style scoped>
    .el-row{
        aligin-items: center;
        min-height:  100vh;
        height: 100%;
        display: flex;
        text-align: center;
    }

    .el-divider{
        height: 450px;
    }

    .codeImg{
        float:left;
        margin-left: 10px;
        border-radius: 5px;
    }
</style>