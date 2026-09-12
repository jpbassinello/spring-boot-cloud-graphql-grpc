import { NextRequest, NextResponse } from "next/server";

const GRAPHQL_URL = process.env.GRAPHQL_URL || "http://localhost:8081/graphql";

export async function POST(request: NextRequest) {
  const body = await request.text();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  const authorization = request.headers.get("Authorization");
  if (authorization) {
    headers["Authorization"] = authorization;
  }

  try {
    const response = await fetch(GRAPHQL_URL, {
      method: "POST",
      headers,
      body,
    });

    const data = await response.json();
    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error("GraphQL proxy error:", error);
    return NextResponse.json(
      {
        errors: [
          {
            message: "Unable to reach GraphQL server",
          },
        ],
      },
      { status: 502 },
    );
  }
}
