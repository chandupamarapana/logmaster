import api from "./api";

export const getAllSuppliers = async () => {
  const response = await api.get("/suppliers");
  return response.data;
};

export const searchSuppliers = async (name) => {
  const response = await api.get(`/suppliers/search?name=${name}`);
  return response.data;
};

export const getAllDeliveries = async (supplierId = null) => {
  const url = supplierId
    ? `/deliveries?supplierId=${supplierId}`
    : "/deliveries";
  const response = await api.get(url);
  return response.data;
};

export const getDeliveryById = async (id) => {
  const response = await api.get(`/deliveries/${id}`);
  return response.data;
};

export const getSummary = async (id) => {
  const response = await api.get(`/deliveries/${id}/summary`);
  return response.data;
};

export const getReportUrl = (id) => {
  return `http://localhost:8080/api/v1/deliveries/${id}/report`;
};
