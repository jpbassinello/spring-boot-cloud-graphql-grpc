import type { CodegenConfig } from "@graphql-codegen/cli";

// The gateway's .graphqls files ARE the schema — codegen reads them straight off disk, so no
// running server is needed and a schema change is a type error at `pnpm codegen` time.
const config: CodegenConfig = {
  schema: "../services/graphql/gateway/src/main/resources/graphql/*.graphqls",
  documents: "src/**/*.tsx",
  generates: {
    "./src/graphql/generated/": {
      preset: "client",
      config: {
        scalars: {
          UUID: "string",
          DateTime: "string",
          Date: "string",
          BigDecimal: "string",
        },
        enumType: "native",
      },
    },
  },
};

export default config;
