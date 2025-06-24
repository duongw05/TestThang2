import { createRouter, createWebHistory } from 'vue-router'
import ProductList from "@/components/Product/ProductList.vue";
import Home from "@/components/Home/Home.vue";
import Dashboard from "@/layout/Dashboard.vue";
import CategoryList from "@/components/Category/CategoryList.vue";
import AddCategory from "@/components/Category/AddCategory.vue";
import UpdateCategory from "@/components/Category/UpdateCategory.vue";
import DetailCategory from "@/components/Category/DetailCategory.vue";
import AddProduct from "@/components/Product/AddProduct.vue";
import UpdateProduct from "@/components/Product/UpdateProduct.vue";
import DetailProduct from "@/components/Product/DetailProduct.vue";

const routes = [
  // Các trang sau khi đăng nhập sẽ có Dashboard
  {
    path: "/",
    component: Dashboard,
    meta: { requiresAuth: true },
    children: [
      { path: "", name: "Home", component: Home },
      { path: "categories", name: "Category", component: CategoryList },
      { path: "categories/add", name: "AddCategory", component: AddCategory },
      { path: "categories/update/:id", name: "UpdateCategory", component: UpdateCategory },
      { path: "categories/detail/:id", name: "DetailCategory", component: DetailCategory },


      { path: "product", name: "ProductList", component: ProductList },
      { path: "product/add", name: "AddProduct", component: AddProduct },
      { path: "product/update/:id", name: "UpdateProduct", component: UpdateProduct },
      { path: "product/detail/:id", name: "DetailProduct", component: DetailProduct }
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});




export default router;