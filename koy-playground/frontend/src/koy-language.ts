import { HighlightStyle, StreamLanguage, syntaxHighlighting } from "@codemirror/language";
import { tags } from "@lezer/highlight";

const keywords = new Set([
  "val", "mutable", "fn", "if", "else", "while", "for", "in", "to",
]);

const literals = new Set(["true", "false", "nil"]);

const builtins = new Set(["println"]);

const koyStreamLanguage = StreamLanguage.define({
  tokenTable: {
    builtin: tags.function(tags.variableName),
  },
  token(stream) {
    if (stream.eatSpace()) return null;

    // Line comment
    if (stream.match("//")) {
      stream.skipToEnd();
      return "lineComment";
    }

    // String literal
    if (stream.match(/"(?:[^"\\]|\\.)*"/)) return "string";

    // Number literal
    if (stream.match(/\d+/)) return "number";

    // Identifier, keyword, literal, builtin
    if (stream.match(/[a-zA-Z_]\w*/)) {
      const word = stream.current();
      if (keywords.has(word)) return "keyword";
      if (literals.has(word)) return "atom";
      if (builtins.has(word)) return "builtin";
      return null;
    }

    // Operators
    if (stream.match(/==|!=|<=|>=|<-|->|\+\+|--|[+\-*/%<>=]/)) return "operator";

    stream.next();
    return null;
  },
});

const koyHighlightStyle = HighlightStyle.define([
  { tag: tags.keyword, color: "#c586c0" },
  { tag: tags.atom, color: "#569cd6" },
  { tag: tags.function(tags.variableName), color: "#dcdcaa" },
  { tag: tags.string, color: "#ce9178" },
  { tag: tags.number, color: "#b5cea8" },
  { tag: tags.lineComment, color: "#6a9955", fontStyle: "italic" },
  { tag: tags.operator, color: "#d4d4d4" },
]);

export const koyLanguage = [
  koyStreamLanguage,
  syntaxHighlighting(koyHighlightStyle),
];
