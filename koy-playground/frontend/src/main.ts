import { basicSetup, EditorView } from "codemirror";
import { EditorState } from "@codemirror/state";
import { keymap } from "@codemirror/view";
import { defaultKeymap } from "@codemirror/commands";

const initialCode = `val x = 10;
val y = 20;
println(x + y);
`;

const editorPane = document.getElementById("editor-pane")!;
const runBtn = document.getElementById("run-btn") as HTMLButtonElement;
const output = document.getElementById("output")!;

const view = new EditorView({
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
    ],
  }),
  parent: editorPane,
});

runBtn.addEventListener("click", async () => {
  const code = view.state.doc.toString();
  runBtn.disabled = true;
  output.textContent = "";
  output.className = "";

  try {
    const res = await fetch("/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });

    const data: { output?: string; error?: string } = await res.json();

    if (res.ok && data.output !== undefined && data.output !== null) {
      output.textContent = data.output;
    } else if (data.error) {
      output.textContent = data.error;
      output.className = "error";
    }
  } catch (e) {
    output.textContent = String(e);
    output.className = "error";
  } finally {
    runBtn.disabled = false;
  }
});
