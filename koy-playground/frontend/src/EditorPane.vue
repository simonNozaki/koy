<script setup lang="ts">
import { defaultKeymap } from "@codemirror/commands";
import { EditorState } from "@codemirror/state";
import { keymap } from "@codemirror/view";
import { basicSetup, EditorView } from "codemirror";
import { onBeforeUnmount, onMounted, ref } from "vue";

const props = defineProps<{ initialCode: string }>();

const emit = defineEmits<{ "update:code": [code: string]; }>();

const editorEl = ref<HTMLElement | null>(null);
const editorView = ref<EditorView | null>(null);

onMounted(() => {
  if (!editorEl.value) return;

  editorView.value = new EditorView({
    state: EditorState.create({
      doc: props.initialCode,
      extensions: [
        basicSetup,
        keymap.of(defaultKeymap),
        EditorView.theme({
          "&": { height: "100%", background: "#1e1e1e" },
          ".cm-content": { caretColor: "#ffffff", fontFamily: '"JetBrains Mono", monospace' },
          ".cm-scroller": { fontFamily: '"JetBrains Mono", monospace' },
          ".cm-gutters": { background: "#1e1e1e", borderRight: "1px solid #3e3e3e" },
        }),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) {
            emit("update:code", update.state.doc.toString());
          }
        }),
      ],
    }),
    parent: editorEl.value,
  });

  emit("update:code", editorView.value.state.doc.toString());
});

onBeforeUnmount(() => {
  editorView.value?.destroy();
  editorView.value = null;
});
</script>

<template>
  <div ref="editorEl" class="editor-pane" />
</template>

<style scoped>
.editor-pane {
  flex: 1;
  overflow: auto;
  border-right: 1px solid #3e3e3e;
}

.editor-pane :deep(.cm-editor) {
  height: 100%;
  font-size: 14px;
}
</style>
