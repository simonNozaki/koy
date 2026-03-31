<script setup lang="ts">
import { onMounted, ref } from "vue";
import { basicSetup, EditorView } from "codemirror";
import { EditorState } from "@codemirror/state";
import { keymap } from "@codemirror/view";
import { defaultKeymap } from "@codemirror/commands";

const emit = defineEmits<{
  "update:code": [code: string];
}>();

const editorEl = ref<HTMLElement | null>(null);

const initialCode = `val x = 10;
val y = 20;
println(x + y);
`;

onMounted(() => {
  if (!editorEl.value) return;

  new EditorView({
    state: EditorState.create({
      doc: initialCode,
      extensions: [
        basicSetup,
        keymap.of(defaultKeymap),
        EditorView.theme({
          "&": { height: "100%", background: "#1e1e1e" },
          ".cm-content": { caretColor: "#aeafad" },
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
