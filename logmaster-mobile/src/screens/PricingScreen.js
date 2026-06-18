import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  ActivityIndicator,
} from "react-native";
import { setPrices } from "../services/deliveryService";

const CATEGORIES = ["LOW", "MID", "HIGH"];
const CATEGORY_LABELS = {
  LOW: "Low (< 23 in)",
  MID: "Mid (23–30 in)",
  HIGH: "High (> 30 in)",
};

const LOG_TYPE_LABELS = {
  RUBBER_4FT: "4ft Rubber",
  OTHER_4FT: "4ft Other",
  RUBBER_3FT: "3ft Rubber",
  OTHER_3FT: "3ft Other",
};

export default function PricingScreen({ route, navigation }) {
  const { deliveryId, logs } = route.params;

  // Find which log types are actually used
  const activeTypes = [...new Set(logs.map((l) => l.logType))];

  // Initialise price state: { RUBBER_4FT: { LOW: '', MID: '', HIGH: '' }, ... }
  const initialPrices = {};
  activeTypes.forEach((type) => {
    initialPrices[type] = { LOW: "", MID: "", HIGH: "" };
  });

  const [prices, setPricesState] = useState(initialPrices);
  const [loading, setLoading] = useState(false);

  const handlePriceChange = (type, category, value) => {
    setPricesState((prev) => ({
      ...prev,
      [type]: { ...prev[type], [category]: value },
    }));
  };

  const handleSubmit = async () => {
    // Build prices array
    const pricesArray = [];
    for (const type of activeTypes) {
      for (const cat of CATEGORIES) {
        const val = prices[type][cat];
        if (val && parseFloat(val) > 0) {
          pricesArray.push({
            logType: type,
            category: cat,
            pricePerCft: parseFloat(val),
          });
        }
      }
    }

    if (pricesArray.length === 0) {
      Alert.alert("Error", "Please enter at least one price");
      return;
    }

    setLoading(true);
    try {
      await setPrices(deliveryId, pricesArray);
      navigation.navigate("Summary", { deliveryId });
    } catch (error) {
      Alert.alert("Error", "Failed to save prices");
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Set Prices per Cft (LKR)</Text>
      <Text style={styles.subtitle}>Only active log types are shown</Text>

      {activeTypes.map((type) => (
        <View key={type} style={styles.typeCard}>
          <Text style={styles.typeLabel}>{LOG_TYPE_LABELS[type]}</Text>

          {CATEGORIES.map((cat) => (
            <View key={cat} style={styles.priceRow}>
              <Text style={styles.catLabel}>{CATEGORY_LABELS[cat]}</Text>
              <TextInput
                style={styles.priceInput}
                placeholder="0.00"
                keyboardType="numeric"
                value={prices[type][cat]}
                onChangeText={(val) => handlePriceChange(type, cat, val)}
              />
            </View>
          ))}
        </View>
      ))}

      <TouchableOpacity
        style={styles.button}
        onPress={handleSubmit}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.buttonText}>Calculate Summary →</Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5", padding: 16 },
  title: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#2E7D32",
    marginBottom: 4,
  },
  subtitle: { fontSize: 12, color: "#888", marginBottom: 20 },
  typeCard: {
    backgroundColor: "#fff",
    borderRadius: 10,
    padding: 16,
    marginBottom: 16,
    elevation: 2,
  },
  typeLabel: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#1B5E20",
    marginBottom: 12,
  },
  priceRow: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 10,
  },
  catLabel: { flex: 1, fontSize: 13, color: "#555" },
  priceInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 6,
    padding: 10,
    fontSize: 14,
    textAlign: "right",
    backgroundColor: "#fafafa",
  },
  button: {
    backgroundColor: "#2E7D32",
    padding: 18,
    borderRadius: 8,
    alignItems: "center",
    marginVertical: 20,
  },
  buttonText: { color: "#fff", fontSize: 16, fontWeight: "bold" },
});
