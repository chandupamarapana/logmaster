import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

// const BASE_URL = 'http://10.0.2.2:8080/api/v1';
// 10.0.2.2 is the Android emulator's way of reaching localhost
const BASE_URL = "http://192.168.1.12:8080/api/v1";
// Change to your Railway URL when deployed

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor — attach JWT token to every request
api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401 globally
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      await AsyncStorage.removeItem("token");
    }
    return Promise.reject(error);
  }
);

export default api;
