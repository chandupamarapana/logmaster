import React, { useState, useEffect } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createStackNavigator } from "@react-navigation/stack";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { ActivityIndicator, View } from "react-native";

import LoginScreen from "../screens/LoginScreen";
import SupplierScreen from "../screens/SupplierScreen";
import LogEntryScreen from "../screens/LogEntryScreen";
import PricingScreen from "../screens/PricingScreen";
import SummaryScreen from "../screens/SummaryScreen";

const Stack = createStackNavigator();

export default function AppNavigator() {
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = await AsyncStorage.getItem("token");
    setIsAuthenticated(token !== null);
    setIsLoading(false);
  };

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
        <ActivityIndicator size="large" color="#2E7D32" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={isAuthenticated ? "Supplier" : "Login"}
        screenOptions={{
          headerStyle: { backgroundColor: "#2E7D32" },
          headerTintColor: "#fff",
          headerTitleStyle: { fontWeight: "bold" },
        }}
      >
        <Stack.Screen
          name="Login"
          component={LoginScreen}
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="Supplier"
          component={SupplierScreen}
          options={{ title: "LogMaster", headerLeft: null }}
        />
        <Stack.Screen
          name="LogEntry"
          component={LogEntryScreen}
          options={{ title: "Enter Logs" }}
        />
        <Stack.Screen
          name="Pricing"
          component={PricingScreen}
          options={{ title: "Set Prices" }}
        />
        <Stack.Screen
          name="Summary"
          component={SummaryScreen}
          options={{ title: "Summary" }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
