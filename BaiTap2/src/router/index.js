import { createRouter, createWebHistory } from 'vue-router'
import ProductList from "@/components/Product/ProductList.vue";
import Home from "@/components/Home/Home.vue";
import Dashboard from "@/layout/Dashboard.vue";
import CategoryList from "@/components/Category/CategoryList.vue";
import CategoryForm from "@/components/Category/CategoryForm.vue";
import ProductForm from "@/components/Product/ProductForm.vue";

const routes = [
  {
    path: "/",
    component: Dashboard,
    meta: { requiresAuth: true },
    children: [
      { path: "", name: "Home", component: Home },
      { path: "categories", name: "Category", component: CategoryList },
      { path: "categories/add", name: "AddCategory", component: CategoryForm,props: {mode: 'add'} },
      { path: "categories/update/:id", name: "UpdateCategory", component: CategoryForm,props: {mode: 'edit'} },
      { path: "categories/detail/:id", name: "DetailCategory", component: CategoryForm,props: {mode: 'view'} },


      { path: "product", name: "ProductList", component: ProductList },
      { path: "product/add", name: "AddProduct", component: ProductForm,props: { mode: 'add' } },
      { path: "product/update/:id", name: "UpdateProduct", component: ProductForm,props: { mode: 'edit' } },
      { path: "product/detail/:id", name: "DetailProduct", component: ProductForm,props: { mode: 'view' } }
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;