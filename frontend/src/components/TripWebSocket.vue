<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { connectTripWebSocket, disconnectTripWebSocket } from "@/services/tripWebSocket";

const trips = ref({});

onMounted(() => {
  connectTripWebSocket((heartbeat) => {
    if (heartbeat.carId === -1) {
      // this heartbeat shows that the trip has ended
      delete trips.value[heartbeat.tripId];
    } else {
      trips.value[heartbeat.tripId] = heartbeat;
    }
  });
});

onUnmounted(() => {
  disconnectTripWebSocket();
});
</script>

<template>
  <div>
    <h1>Live Trips</h1>
    <div v-if="Object.keys(trips).length === 0">
      <div class="no-trips">No trips yet!</div>
    </div>
    <div v-else>
      <div class="trips" v-for="trip in trips" :key="trip.tripId">
        <h2>Trip: {{ trip.tripId }}</h2>
        🚗 {{ trip.carId }} - 👦 {{ trip.driverId }} <br/>
        📈 {{ trip.speed }} km/h <br/>
        ⌖ lat: {{ trip.latitude }}, lon: {{ trip.longitude }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-trips {
  color: red;
}
.trips {
  border: 1px solid lightgray;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 5px;
}
</style>


