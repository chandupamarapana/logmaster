import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
} from "react-native";
import { getSummary } from "../services/deliveryService";
import { BASE_URL } from "../services/api";
import * as FileSystem from "expo-file-system/legacy";
import * as Sharing from "expo-sharing";
import AsyncStorage from "@react-native-async-storage/async-storage";

const LOG_TYPE_LABELS = {
  RUBBER_4FT: "4ft Rubber",
  OTHER_4FT: "4ft Other",
  RUBBER_3FT: "3ft Rubber",
  OTHER_3FT: "3ft Other",
};

const CATEGORY_LABELS = {
  LOW: "Low",
  MID: "Mid",
  HIGH: "High",
};

export default function SummaryScreen({ route, navigation }) {
  const { deliveryId } = route.params;
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    loadSummary();
  }, []);

  const loadSummary = async () => {
    try {
      const data = await getSummary(deliveryId);
      setSummary(data);
    } catch (error) {
      Alert.alert("Error", "Failed to load summary");
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPDF = async () => {
    setDownloading(true);
    try {
      const token = await AsyncStorage.getItem("token");
      console.log("Token:", token ? "exists" : "missing");

      const url = `${BASE_URL}/deliveries/${deliveryId}/report`;
      console.log("URL:", url);

      const fileUri =
        FileSystem.documentDirectory + `LogMaster_${deliveryId}.pdf`;
      console.log("File URI:", fileUri);

      const downloadResult = await FileSystem.downloadAsync(url, fileUri, {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log("Download status:", downloadResult.status);
      console.log("Download URI:", downloadResult.uri);

      if (downloadResult.status !== 200) {
        Alert.alert("Error", `Server returned ${downloadResult.status}`);
        return;
      }

      await Sharing.shareAsync(downloadResult.uri, {
        mimeType: "application/pdf",
        dialogTitle: "LogMaster Report",
      });
    } catch (error) {
      console.log("PDF Error:", error.message);
      Alert.alert("Error", error.message);
    } finally {
      setDownloading(false);
    }
  };

  const handleNewDelivery = () => {
    navigation.navigate("Supplier");
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#2E7D32" />
        <Text style={styles.loadingText}>Calculating...</Text>
      </View>
    );
  }

  if (!summary) {
    return (
      <View style={styles.centered}>
        <Text>Failed to load summary</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      {/* Header */}
      <View style={styles.headerCard}>
        <Text style={styles.supplierName}>{summary.supplierName}</Text>
        <Text style={styles.date}>{summary.deliveryDate}</Text>
        <Text style={styles.thresholds}>
          LOW &lt;{summary.categoryThresholds.lowMax} | MID{" "}
          {summary.categoryThresholds.lowMax}–
          {summary.categoryThresholds.midMax} | HIGH &gt;
          {summary.categoryThresholds.midMax}
        </Text>
      </View>

      {/* Per type breakdown */}
      {Object.entries(summary.byType).map(([type, typeData]) => (
        <View key={type} style={styles.typeCard}>
          <Text style={styles.typeTitle}>{LOG_TYPE_LABELS[type] || type}</Text>

          <View style={styles.tableHeader}>
            <Text style={styles.col1}>Category</Text>
            <Text style={styles.col2}>Logs</Text>
            <Text style={styles.col2}>Cft</Text>
            <Text style={styles.col2}>LKR</Text>
          </View>

          {Object.entries(typeData.categories).map(([cat, catData]) => (
            <View key={cat} style={styles.tableRow}>
              <Text style={styles.col1}>{CATEGORY_LABELS[cat] || cat}</Text>
              <Text style={styles.col2}>{catData.logCount}</Text>
              <Text style={styles.col2}>
                {parseFloat(catData.totalCft).toFixed(3)}
              </Text>
              <Text style={styles.col2}>
                {parseFloat(catData.totalPrice).toFixed(2)}
              </Text>
            </View>
          ))}

          <View style={styles.subtotalRow}>
            <Text style={styles.col1}>Subtotal</Text>
            <Text style={styles.col2}>{typeData.totalLogs}</Text>
            <Text style={styles.col2}>
              {parseFloat(typeData.totalCft).toFixed(3)}
            </Text>
            <Text style={styles.col2}>
              {parseFloat(typeData.totalPrice).toFixed(2)}
            </Text>
          </View>
        </View>
      ))}

      {/* Grand Total */}
      <View style={styles.grandTotalCard}>
        <Text style={styles.grandTotalTitle}>GRAND TOTAL</Text>
        <View style={styles.grandTotalRow}>
          <Text style={styles.grandTotalLabel}>Total Logs</Text>
          <Text style={styles.grandTotalValue}>{summary.grandTotalLogs}</Text>
        </View>
        <View style={styles.grandTotalRow}>
          <Text style={styles.grandTotalLabel}>Total Cft</Text>
          <Text style={styles.grandTotalValue}>
            {parseFloat(summary.grandTotalCft).toFixed(4)}
          </Text>
        </View>
        <View style={[styles.grandTotalRow, styles.totalAmountRow]}>
          <Text style={styles.totalAmountLabel}>Total Amount</Text>
          <Text style={styles.totalAmountValue}>
            LKR {parseFloat(summary.grandTotalPrice).toFixed(2)}
          </Text>
        </View>
      </View>

      {/* Download PDF Button */}
      <TouchableOpacity
        style={styles.downloadBtn}
        onPress={handleDownloadPDF}
        disabled={downloading}
      >
        {downloading ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.downloadBtnText}>📄 Download PDF Invoice</Text>
        )}
      </TouchableOpacity>

      {/* New Delivery Button */}
      <TouchableOpacity
        style={styles.newDeliveryBtn}
        onPress={handleNewDelivery}
      >
        <Text style={styles.newDeliveryText}>+ Start New Delivery</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#f5f5f5", padding: 16 },
  centered: { flex: 1, justifyContent: "center", alignItems: "center" },
  loadingText: { marginTop: 12, color: "#666" },
  headerCard: {
    backgroundColor: "#2E7D32",
    borderRadius: 10,
    padding: 16,
    marginBottom: 16,
  },
  supplierName: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#fff",
    marginBottom: 4,
  },
  date: { fontSize: 14, color: "#c8e6c9", marginBottom: 4 },
  thresholds: { fontSize: 11, color: "#a5d6a7" },
  typeCard: {
    backgroundColor: "#fff",
    borderRadius: 10,
    padding: 14,
    marginBottom: 12,
    elevation: 2,
  },
  typeTitle: {
    fontSize: 15,
    fontWeight: "bold",
    color: "#1B5E20",
    marginBottom: 10,
  },
  tableHeader: {
    flexDirection: "row",
    marginBottom: 6,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
    paddingBottom: 4,
  },
  tableRow: { flexDirection: "row", paddingVertical: 4 },
  subtotalRow: {
    flexDirection: "row",
    paddingVertical: 6,
    borderTopWidth: 1,
    borderTopColor: "#eee",
    marginTop: 4,
  },
  col1: { flex: 2, fontSize: 12, color: "#333", fontWeight: "500" },
  col2: { flex: 1, fontSize: 12, color: "#333", textAlign: "right" },
  grandTotalCard: {
    backgroundColor: "#1B5E20",
    borderRadius: 10,
    padding: 16,
    marginBottom: 16,
  },
  grandTotalTitle: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#fff",
    marginBottom: 12,
    textAlign: "center",
  },
  grandTotalRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  grandTotalLabel: { fontSize: 14, color: "#c8e6c9" },
  grandTotalValue: { fontSize: 14, color: "#fff", fontWeight: "600" },
  totalAmountRow: {
    borderTopWidth: 1,
    borderTopColor: "#388E3C",
    paddingTop: 10,
    marginTop: 4,
  },
  totalAmountLabel: { fontSize: 16, color: "#fff", fontWeight: "bold" },
  totalAmountValue: { fontSize: 18, color: "#69F0AE", fontWeight: "bold" },
  downloadBtn: {
    backgroundColor: "#1565C0",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginBottom: 12,
  },
  downloadBtnText: { color: "#fff", fontSize: 15, fontWeight: "bold" },
  newDeliveryBtn: {
    backgroundColor: "#2E7D32",
    padding: 16,
    borderRadius: 8,
    alignItems: "center",
    marginBottom: 30,
  },
  newDeliveryText: { color: "#fff", fontSize: 15, fontWeight: "bold" },
});
