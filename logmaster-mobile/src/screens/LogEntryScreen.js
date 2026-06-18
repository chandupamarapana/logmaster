import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  Alert,
  ActivityIndicator,
} from "react-native";
import { addLogEntry, deleteLogEntry } from "../services/deliveryService";

const LOG_TYPES = [
  { key: "RUBBER_4FT", label: "4ft Rubber" },
  { key: "OTHER_4FT", label: "4ft Other" },
  { key: "RUBBER_3FT", label: "3ft Rubber" },
  { key: "OTHER_3FT", label: "3ft Other" },
];

export default function LogEntryScreen({ route, navigation }) {
  const { deliveryId, supplierName, date } = route.params;

  const [selectedType, setSelectedType] = useState("RUBBER_4FT");
  const [input, setInput] = useState("");
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleNumber = (num) => {
    if (input.length >= 5) return;
    setInput((prev) => prev + num);
  };

  const handleDecimal = () => {
    if (input.includes(".")) return;
    setInput((prev) => (prev === "" ? "0." : prev + "."));
  };

  const handleClear = () => setInput("");

  const handleDelete = () => {
    setInput((prev) => prev.slice(0, -1));
  };

  const handleAdd = async () => {
    if (!input || parseFloat(input) <= 0) {
      Alert.alert("Error", "Please enter a valid circumference");
      return;
    }

    setSaving(true);
    try {
      const entry = await addLogEntry(deliveryId, selectedType, input);
      setLogs((prev) => [...prev, entry]);
      setInput("");
    } catch (error) {
      Alert.alert("Error", "Failed to add log entry");
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async (logId) => {
    try {
      await deleteLogEntry(deliveryId, logId);
      setLogs((prev) => prev.filter((l) => l.id !== logId));
    } catch (error) {
      Alert.alert("Error", "Failed to delete log entry");
    }
  };

  const handleNext = () => {
    if (logs.length === 0) {
      Alert.alert("Error", "Please add at least one log entry");
      return;
    }
    navigation.navigate("Pricing", { deliveryId, logs });
  };

  return (
    <View style={styles.container}>
      {/* Header info */}
      <Text style={styles.header}>
        {supplierName} — {date}
      </Text>

      {/* Log type selector */}
      <View style={styles.typeRow}>
        {LOG_TYPES.map((type) => (
          <TouchableOpacity
            key={type.key}
            style={[
              styles.typeBtn,
              selectedType === type.key && styles.typeBtnActive,
            ]}
            onPress={() => setSelectedType(type.key)}
          >
            <Text
              style={[
                styles.typeBtnText,
                selectedType === type.key && styles.typeBtnTextActive,
              ]}
            >
              {type.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Display */}
      <View style={styles.display}>
        <Text style={styles.displayText}>{input || "0"} in</Text>
      </View>

      {/* Log list */}
      <FlatList
        data={logs.slice().reverse()}
        keyExtractor={(item) => item.id.toString()}
        style={styles.logList}
        renderItem={({ item }) => (
          <View style={styles.logItem}>
            <Text style={styles.logText}>
              {item.logType} — {item.circumference} in → {item.cubicFeet} Cft (
              {item.category})
            </Text>
            <TouchableOpacity onPress={() => handleRemove(item.id)}>
              <Text style={styles.removeText}>✕</Text>
            </TouchableOpacity>
          </View>
        )}
        ListEmptyComponent={
          <Text style={styles.emptyText}>No logs added yet</Text>
        }
      />

      {/* Calculator */}
      <View style={styles.calculator}>
        {[
          ["7", "8", "9"],
          ["4", "5", "6"],
          ["1", "2", "3"],
          [".", "0", "⌫"],
        ].map((row, i) => (
          <View key={i} style={styles.calcRow}>
            {row.map((key) => (
              <TouchableOpacity
                key={key}
                style={styles.calcBtn}
                onPress={() => {
                  if (key === "⌫") handleDelete();
                  else if (key === ".") handleDecimal();
                  else handleNumber(key);
                }}
              >
                <Text style={styles.calcBtnText}>{key}</Text>
              </TouchableOpacity>
            ))}
          </View>
        ))}

        <View style={styles.calcRow}>
          <TouchableOpacity
            style={[styles.calcBtn, styles.clearBtn]}
            onPress={handleClear}
          >
            <Text style={styles.calcBtnText}>C</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.calcBtn, styles.addBtn, { flex: 2 }]}
            onPress={handleAdd}
            disabled={saving}
          >
            {saving ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.calcBtnText}>ADD</Text>
            )}
          </TouchableOpacity>
        </View>
      </View>

      {/* Next button */}
      <TouchableOpacity style={styles.nextBtn} onPress={handleNext}>
        <Text style={styles.nextBtnText}>
          Next: Set Prices ({logs.length} logs) →
        </Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5" },
  header: {
    fontSize: 14,
    color: "#666",
    padding: 12,
    backgroundColor: "#fff",
    textAlign: "center",
  },
  typeRow: {
    flexDirection: "row",
    padding: 8,
    backgroundColor: "#fff",
    marginBottom: 4,
  },
  typeBtn: {
    flex: 1,
    padding: 8,
    margin: 3,
    borderRadius: 6,
    backgroundColor: "#eee",
    alignItems: "center",
  },
  typeBtnActive: { backgroundColor: "#2E7D32" },
  typeBtnText: { fontSize: 11, color: "#333", fontWeight: "600" },
  typeBtnTextActive: { color: "#fff" },
  display: {
    backgroundColor: "#1B5E20",
    padding: 16,
    alignItems: "flex-end",
    paddingRight: 20,
  },
  displayText: { fontSize: 32, color: "#fff", fontWeight: "bold" },
  logList: { flex: 1, padding: 8, maxHeight: 120 },
  logItem: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#fff",
    padding: 10,
    borderRadius: 6,
    marginBottom: 4,
  },
  logText: { fontSize: 12, color: "#333", flex: 1 },
  removeText: { color: "#c62828", fontSize: 16, paddingLeft: 8 },
  emptyText: { color: "#999", textAlign: "center", padding: 10, fontSize: 12 },
  calculator: { backgroundColor: "#fff", padding: 8 },
  calcRow: { flexDirection: "row", marginBottom: 4 },
  calcBtn: {
    flex: 1,
    margin: 3,
    padding: 16,
    backgroundColor: "#e0e0e0",
    borderRadius: 8,
    alignItems: "center",
  },
  clearBtn: { backgroundColor: "#ef9a9a" },
  addBtn: { backgroundColor: "#2E7D32" },
  calcBtnText: { fontSize: 18, fontWeight: "bold", color: "#333" },
  nextBtn: {
    backgroundColor: "#1B5E20",
    padding: 16,
    alignItems: "center",
    margin: 8,
    borderRadius: 8,
  },
  nextBtnText: { color: "#fff", fontWeight: "bold", fontSize: 14 },
});
