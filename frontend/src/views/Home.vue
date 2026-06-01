<template>
    <el-container>
        <el-aside width="200px">
            <side-menu></side-menu>
        </el-aside>

        <el-container>
            <el-header>
                <div style="float:left;position:relative;left:45%">
                    <strong>VueManager后台管理系统</strong>
                </div>
                <div class="header-avatar">
                    <el-avatar :src="userInfo.avatar"/>

                    <el-dropdown>
            <span class="el-dropdown-link">
              {{userInfo.username}}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
                        <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item>
                                <router-link :to="{name:'UserCenter'}">个人中心</router-link>
                            </el-dropdown-item>
                            <el-dropdown-item @click.native="logout">退出</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>
                </div>
            </el-header>

            <el-main>
                <tabs></tabs>
                <div style="margin: 0 15px;">
                    <router-view/>
                </div>
            </el-main>

        </el-container>
    </el-container>
</template>

<script>
    import SideMenu from "./inc/SideMenu";
    import Tabs from "./inc/Tabs";

    export default  {
        name:"Home",
        components: {
            SideMenu,Tabs
        },
        data(){
            return {
                userInfo: {
                    id:"",
                    username:"",
                    avatar:"",
                }
            }
        },
        created() {
            this.getUserInfo()
        },
        methods:{
            getUserInfo(){
                this.$axios.get('/sys/userInfo').then(res => {
                    this.userInfo = res.data.data
                })
            },
            logout(){
                this.$axios.post("/logout").then(res =>{
                    localStorage.clear()
                    sessionStorage.clear()

                    this.$store.commit("resetState")

                    this.$router.push("/login").catch(() => {});
                })
            }
        }
    }
</script>

<style scoped>

    .el-container{
        padding: 0;
        margin: 0;
        display:flex;
        min-height: 100vh;
    }

    .el-header {
        background-color: #17B3A3;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0 20px;
        height: 60px;
    }

    .header-avatar {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;
    }

    .el-aside {
        background-color: #D3DCE6;
        color: #333;
        text-align: left;
        line-height: 200px;
    }

    .el-main {
        color: #333;
        text-align: left;
        /*line-height: 160px;*/
        padding: 0;
    }

</style>