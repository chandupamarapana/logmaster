import api from "./api";

export const login = async (username, password) => {
  const response = await api.post("/auth/login", { username, password });
  const token = response.data.token;
  localStorage.setItem("token", token);
  return token;
};

export const logout = () => {
  localStorage.removeItem("token");
};

export const isLoggedIn = () => {
  return localStorage.getItem("token") !== null;
};
