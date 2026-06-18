import api from "./api";

export const createSupplier = async (name) => {
  const response = await api.post("/suppliers", { name });
  return response.data;
};

export const searchSuppliers = async (name) => {
  const response = await api.get(`/suppliers/search?name=${name}`);
  return response.data;
};

export const createDelivery = async (
  supplierId,
  deliveryDate,
  categoryLowMax = 23,
  categoryMidMax = 30
) => {
  const response = await api.post("/deliveries", {
    supplierId,
    deliveryDate,
    categoryLowMax,
    categoryMidMax,
  });
  return response.data;
};

export const addLogEntry = async (deliveryId, logType, circumference) => {
  const response = await api.post(`/deliveries/${deliveryId}/logs`, {
    logType,
    circumference: String(circumference),
  });
  return response.data;
};

export const getLogEntries = async (deliveryId) => {
  const response = await api.get(`/deliveries/${deliveryId}/logs`);
  return response.data;
};

export const deleteLogEntry = async (deliveryId, logId) => {
  await api.delete(`/deliveries/${deliveryId}/logs/${logId}`);
};

export const setPrices = async (deliveryId, prices) => {
  const response = await api.put(`/deliveries/${deliveryId}/prices`, prices);
  return response.data;
};

export const getSummary = async (deliveryId) => {
  const response = await api.get(`/deliveries/${deliveryId}/summary`);
  return response.data;
};
