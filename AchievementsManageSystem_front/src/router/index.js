import {createRouter, createWebHistory} from 'vue-router'
import {useRoute, useRouter} from "vue-router"
import ParkingDiscoveryNew  from "../views/Parking/ParkingDiscoveryNew.vue";
import DataInsights from "../views/Parking/DataInsights.vue";
const routes = [
    {
        path: "/LoginAndRegister",
        name: "loginandregister",
        component: () => import('../views/System/LoginAndRegister.vue'),
    },
    {
        path: "/",
        name: "layout",
        redirect: "/parking-discovery-new",
        component: () => import('../layout/layout.vue'),
        children: [
            // {
            //     path: "home",
            //     name: "Home",
            //     component: () => import('../views/System/Home.vue'),
            // },
            // {
            //     path: "parking-discovery",
            //     name: "ParkingDiscovery",
            //     component: () => import('../views/Parking/ParkingDiscovery.vue'),
            // },
            {
                path: "parking-discovery-new",
                name: "ParkingDiscoveryNew",
                component: ParkingDiscoveryNew,
            },
            // {
            //     path: "parking-discovery-new2",
            //     name: "ParkingDiscoveryNew2",
            //     component: () => import('../views/Parking/ParkingDiscoveryNew2.vue'),
            // },
            // {
            //     path: "parking-prediction",
            //     name: "ParkingPrediction",
            //     component: () => import('../views/Parking/ParkingPrediction.vue'),
            // },
            {
                path: "data-insights",
                name: "DataInsights",
                component:DataInsights,
            }
        ]
    },
];
const router = createRouter({
    history: createWebHistory(), routes,
})
// 导航守卫
router.beforeEach((to, from, next) => {

    const token = sessionStorage.getItem("saToken")
    const role = sessionStorage.getItem('role')
    const requireAdmin = to.meta.requireAdmin;//要访问的页面是否需要admin角色
    console.log("requireAdmin is ", token)
    if (token) {// 如果存在token，则允许访问路由
            next();
    } else { // 如果不存在token，则跳转到登录页面或注册页面
        if (to.name === 'loginandregister' || to.name === 'passwordRecovery' || to.name === 'wsChart') {
            next()
        } else {
            next({name: "loginandregister"})
        }
    }

})


export default router
