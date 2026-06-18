import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ActivityIndicator,
  Platform,
} from "react-native";
import { createSupplier, createDelivery } from "../services/deliveryService";
import { logout } from "../services/authService";

export default function SupplierScreen({ navigation }) {
  const [supplierName, setSupplierName] = useState("");
  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [loading, setLoading] = useState(false);

  const handleStart = async () => {
    if (!supplierName.trim()) {
      Alert.alert("Error", "Please enter supplier name");
      return;
    }

    setLoading(true);
    try {
      // Create supplier
      const supplier = await createSupplier(supplierName.trim());

      // Create delivery session
      const delivery = await createDelivery(supplier.id, date);

      // Navigate to log entry with delivery info
      navigation.navigate("LogEntry", {
        deliveryId: delivery.id,
        supplierName: supplier.name,
        date: date,
      });
    } catch (error) {
      Alert.alert("Error", "Failed to start delivery session");
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigation.replace("Login");
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
        <Text style={styles.logoutText}>Logout</Text>
      </TouchableOpacity>

      <Text style={styles.title}>New Delivery</Text>
      <Text style={styles.label}>Supplier Name</Text>
      <TextInput
        style={styles.input}
        placeholder="Enter supplier name"
        value={supplierName}
        onChangeText={setSupplierName}
      />

      <Text style={styles.label}>Date</Text>
      <TextInput
        style={styles.input}
        placeholder="YYYY-MM-DD"
        value={date}
        onChangeText={setDate}
      />

      <TouchableOpacity
        style={styles.button}
        onPress={handleStart}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.buttonText}>Start Entering Logs →</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    backgroundColor: "#f5f5f5",
  },
  logoutBtn: {
    alignSelf: "flex-end",
    marginBottom: 20,
  },
  logoutText: {
    color: "#c62828",
    fontSize: 14,
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#2E7D32",
    marginBottom: 30,
  },
  label: {
    fontSize: 14,
    fontWeight: "600",
    color: "#333",
    marginBottom: 6,
  },
  input: {
    backgroundColor: "#fff",
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 8,
    padding: 15,
    marginBottom: 20,
    fontSize: 16,
  },
  button: {
    backgroundColor: "#2E7D32",
    padding: 18,
    borderRadius: 8,
    alignItems: "center",
    marginTop: 10,
  },
  buttonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "bold",
  },
});
