<script setup lang="ts">
import { ref } from "vue";
import EditorPane from "./EditorPane.vue";
import OutputPane from "./OutputPane.vue";

const initialCode = `val x = 10;
val y = 20;
println(x + y);
`;

const code = ref<string>(initialCode);
const output = ref<string | null>(null);
const error = ref<string | null>(null);
const loading = ref(false);

async function run() {
  loading.value = true;
  output.value = null;
  error.value = null;

  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), 15000);

  try {
    const res = await fetch("/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: code.value }),
      signal: controller.signal,
    });
    clearTimeout(timeoutId);

    const data: { output?: string; error?: string } = await res.json();

    if (res.ok && data.output !== undefined && data.output !== null) {
      output.value = data.output;
    } else if (data.error) {
      error.value = data.error;
    } else {
      error.value = `Request failed with status ${res.status}`;
    }
  } catch (e) {
    clearTimeout(timeoutId);
    error.value = String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <header>
    <h1>Koy Playground</h1>
    <button :disabled="loading" @click="run">▶ Run</button>
  </header>
  <main>
    <EditorPane :initial-code="initialCode" @update:code="code = $event" />
    <OutputPane :output="output" :error="error" />
  </main>
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: monospace;
  background: #1e1e1e;
  color: #d4d4d4;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

header {
  padding: 12px 20px;
  background: #2d2d2d;
  border-bottom: 1px solid #3e3e3e;
  display: flex;
  align-items: center;
  gap: 16px;
}

header h1 {
  font-size: 16px;
  color: #9cdcfe;
}

button {
  padding: 6px 16px;
  background: #0e639c;
  color: #fff;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 13px;
  font-family: monospace;
}

button:hover { background: #1177bb; }
button:focus-visible { outline: 2px solid #9cdcfe; outline-offset: 2px; }
button:disabled { opacity: 0.5; cursor: not-allowed; }

main {
  display: flex;
  flex: 1;
  overflow: hidden;
}
</style>
