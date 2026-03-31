<script setup lang="ts">
defineProps<{
  output: string | null;
  error: string | null;
}>();
</script>

<template>
  <div class="output-pane">
    <h2>Output</h2>
    <pre :class="{ error: !!error }">{{ error || output || "" }}</pre>
  </div>
</template>

<style scoped>
.output-pane {
  flex: 1;
  padding: 16px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.output-pane h2 {
  font-size: 12px;
  color: #808080;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

pre {
  flex: 1;
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.6;
}

pre.error {
  color: #f48771;
}
</style>
