import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getSummary, getReportUrl } from "../services/deliveryService";

const LOG_TYPE_LABELS = {
  RUBBER_4FT: "4ft Rubber",
  OTHER_4FT: "4ft Other",
  RUBBER_3FT: "3ft Rubber",
  OTHER_3FT: "3ft Other",
};

const CATEGORY_LABELS = { LOW: "Low", MID: "Mid", HIGH: "High" };

export default function DeliveryDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSummary();
  }, [id]);

  const loadSummary = async () => {
    try {
      const data = await getSummary(id);
      setSummary(data);
    } catch (error) {
      console.error("Failed to load summary", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPDF = async () => {
    const token = localStorage.getItem("token");
    const url = getReportUrl(id);

    const response = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = downloadUrl;
    a.download = `LogMaster_${id}.pdf`;
    a.click();
  };

  if (loading) return <p style={styles.loading}>Loading...</p>;
  if (!summary) return <p style={styles.loading}>Delivery not found</p>;

  return (
    <div style={styles.container}>
      <button style={styles.backBtn} onClick={() => navigate("/")}>
        ← Back to Dashboard
      </button>

      <div style={styles.headerCard}>
        <h2 style={styles.supplierName}>{summary.supplierName}</h2>
        <p style={styles.date}>{summary.deliveryDate}</p>
        <p style={styles.thresholds}>
          LOW &lt; {summary.categoryThresholds.lowMax} | MID{" "}
          {summary.categoryThresholds.lowMax}–
          {summary.categoryThresholds.midMax} | HIGH &gt;{" "}
          {summary.categoryThresholds.midMax}
        </p>
      </div>

      {Object.entries(summary.byType).map(([type, typeData]) => (
        <div key={type} style={styles.typeCard}>
          <h3 style={styles.typeTitle}>{LOG_TYPE_LABELS[type] || type}</h3>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Category</th>
                <th style={styles.th}>Logs</th>
                <th style={styles.th}>Total Cft</th>
                <th style={styles.th}>Price/Cft</th>
                <th style={styles.th}>Total (LKR)</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(typeData.categories).map(([cat, catData]) => (
                <tr key={cat}>
                  <td style={styles.td}>{CATEGORY_LABELS[cat] || cat}</td>
                  <td style={styles.td}>{catData.logCount}</td>
                  <td style={styles.td}>
                    {parseFloat(catData.totalCft).toFixed(4)}
                  </td>
                  <td style={styles.td}>
                    {catData.pricePerCft
                      ? parseFloat(catData.pricePerCft).toFixed(2)
                      : "—"}
                  </td>
                  <td style={styles.td}>
                    {parseFloat(catData.totalPrice).toFixed(2)}
                  </td>
                </tr>
              ))}
              <tr style={styles.subtotalRow}>
                <td style={styles.td}>
                  <b>Subtotal</b>
                </td>
                <td style={styles.td}>
                  <b>{typeData.totalLogs}</b>
                </td>
                <td style={styles.td}>
                  <b>{parseFloat(typeData.totalCft).toFixed(4)}</b>
                </td>
                <td style={styles.td}></td>
                <td style={styles.td}>
                  <b>{parseFloat(typeData.totalPrice).toFixed(2)}</b>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      ))}

      <div style={styles.grandTotalCard}>
        <h3 style={styles.grandTotalTitle}>GRAND TOTAL</h3>
        <div style={styles.grandRow}>
          <span>Total Logs</span>
          <span>{summary.grandTotalLogs}</span>
        </div>
        <div style={styles.grandRow}>
          <span>Total Cft</span>
          <span>{parseFloat(summary.grandTotalCft).toFixed(4)}</span>
        </div>
        <div style={styles.totalAmountRow}>
          <span>Total Amount</span>
          <span>LKR {parseFloat(summary.grandTotalPrice).toFixed(2)}</span>
        </div>
      </div>

      <button style={styles.downloadBtn} onClick={handleDownloadPDF}>
        📄 Download PDF Invoice
      </button>
    </div>
  );
}

const styles = {
  container: { padding: "30px", maxWidth: "800px", margin: "0 auto" },
  loading: { padding: "30px", color: "#888" },
  backBtn: {
    background: "none",
    border: "none",
    color: "#2E7D32",
    fontSize: "14px",
    cursor: "pointer",
    marginBottom: "20px",
    padding: 0,
  },
  headerCard: {
    backgroundColor: "#2E7D32",
    borderRadius: "10px",
    padding: "20px",
    color: "#fff",
    marginBottom: "20px",
  },
  supplierName: { margin: "0 0 4px 0", fontSize: "22px" },
  date: { margin: "0 0 8px 0", color: "#c8e6c9" },
  thresholds: { margin: 0, fontSize: "12px", color: "#a5d6a7" },
  typeCard: {
    backgroundColor: "#fff",
    borderRadius: "10px",
    padding: "20px",
    marginBottom: "16px",
    boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
  },
  typeTitle: { color: "#1B5E20", marginTop: 0 },
  table: { width: "100%", borderCollapse: "collapse" },
  th: {
    textAlign: "left",
    padding: "8px",
    fontSize: "12px",
    color: "#888",
    borderBottom: "2px solid #eee",
  },
  td: { padding: "8px", fontSize: "13px", borderBottom: "1px solid #f0f0f0" },
  subtotalRow: { backgroundColor: "#f5f5f5" },
  grandTotalCard: {
    backgroundColor: "#1B5E20",
    borderRadius: "10px",
    padding: "20px",
    color: "#fff",
    marginBottom: "20px",
  },
  grandTotalTitle: { marginTop: 0, textAlign: "center" },
  grandRow: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "8px",
    color: "#c8e6c9",
  },
  totalAmountRow: {
    display: "flex",
    justifyContent: "space-between",
    borderTop: "1px solid #388E3C",
    paddingTop: "10px",
    marginTop: "8px",
    fontWeight: "bold",
    fontSize: "18px",
  },
  downloadBtn: {
    width: "100%",
    padding: "14px",
    backgroundColor: "#1565C0",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    fontSize: "15px",
    fontWeight: "bold",
    cursor: "pointer",
    marginBottom: "30px",
  },
};
