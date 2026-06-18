import AsyncStorage from "@react-native-async-storage/async-storage";
import api from "./api";

export const login = async (username, password) => {
  const response = await api.post("/auth/login", { username, password });
  const token = response.data.token;
  await AsyncStorage.setItem("token", token);
  return token;
};

export const logout = async () => {
  await AsyncStorage.removeItem("token");
};

export const isLoggedIn = async () => {
  const token = await AsyncStorage.getItem("token");
  return token !== null;
};
