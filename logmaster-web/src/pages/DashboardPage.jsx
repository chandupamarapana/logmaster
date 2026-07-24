import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getAllDeliveries, searchSuppliers } from "../services/deliveryService";
import { logout } from "../services/authService";

export default function DashboardPage() {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    loadDeliveries();
  }, []);

  const loadDeliveries = async () => {
    try {
      const data = await getAllDeliveries();
      setDeliveries(data);
    } catch (error) {
      console.error("Failed to load deliveries", error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const filteredDeliveries = deliveries.filter((d) =>
    d.supplierName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.title}>LogMaster Dashboard</h1>
        <button style={styles.logoutBtn} onClick={handleLogout}>
          Logout
        </button>
      </header>

      <div style={styles.searchBar}>
        <input
          style={styles.searchInput}
          type="text"
          placeholder="Search by supplier name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {loading ? (
        <p style={styles.loadingText}>Loading deliveries...</p>
      ) : filteredDeliveries.length === 0 ? (
        <p style={styles.emptyText}>No deliveries found</p>
      ) : (
        <div style={styles.grid}>
          {filteredDeliveries.map((delivery) => (
            <div
              key={delivery.id}
              style={styles.card}
              onClick={() => navigate(`/delivery/${delivery.id}`)}
            >
              <h3 style={styles.cardTitle}>{delivery.supplierName}</h3>
              <p style={styles.cardDate}>{delivery.deliveryDate}</p>
              <span style={getStatusStyle(delivery.status)}>
                {delivery.status}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function getStatusStyle(status) {
  const base = {
    padding: "4px 10px",
    borderRadius: "12px",
    fontSize: "11px",
    fontWeight: "bold",
  };
  const colors = {
    DRAFT: { backgroundColor: "#fff3e0", color: "#e65100" },
    PRICED: { backgroundColor: "#e3f2fd", color: "#1565c0" },
    COMPLETED: { backgroundColor: "#e8f5e9", color: "#2e7d32" },
  };
  return { ...base, ...(colors[status] || {}) };
}

const styles = {
  container: {
    padding: "30px",
    backgroundColor: "#f5f5f5",
    minHeight: "100vh",
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "30px",
  },
  title: { fontSize: "26px", color: "#1B5E20", margin: 0 },
  logoutBtn: {
    padding: "8px 16px",
    backgroundColor: "#fff",
    border: "1px solid #c62828",
    color: "#c62828",
    borderRadius: "6px",
    cursor: "pointer",
  },
  searchBar: { marginBottom: "24px" },
  searchInput: {
    width: "100%",
    maxWidth: "400px",
    padding: "12px",
    border: "1px solid #ddd",
    borderRadius: "6px",
    fontSize: "14px",
    boxSizing: "border-box",
  },
  loadingText: { color: "#888" },
  emptyText: { color: "#888" },
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))",
    gap: "16px",
  },
  card: {
    backgroundColor: "#fff",
    borderRadius: "10px",
    padding: "20px",
    boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
    cursor: "pointer",
    transition: "transform 0.15s",
  },
  cardTitle: { fontSize: "16px", color: "#1B5E20", margin: "0 0 8px 0" },
  cardDate: { fontSize: "13px", color: "#888", margin: "0 0 12px 0" },
};
