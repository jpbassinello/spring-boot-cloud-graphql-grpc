"use client";

import * as React from "react";
import { useCallback } from "react";
import { Checkbox as CheckboxPrimitive } from "radix-ui";
import { Check } from "lucide-react";
import { cn, formatLabel as defaultFormatLabel } from "@/lib/utils";

interface CheckboxGroupProps {
  label: string;
  options: string[];
  selected: string[];
  onSelectedChange: (selected: string[]) => void;
  columns?: number;
  formatLabel?: (value: string) => string;
}

export function CheckboxGroup({
  label,
  options,
  selected,
  onSelectedChange,
  columns = 2,
  formatLabel = defaultFormatLabel,
}: CheckboxGroupProps) {
  const handleToggle = useCallback(
    (value: string) => {
      if (selected.includes(value)) {
        onSelectedChange(selected.filter((s) => s !== value));
      } else {
        onSelectedChange([...selected, value]);
      }
    },
    [selected, onSelectedChange],
  );

  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">{label}</label>
      <div
        className={cn("grid gap-2", columns === 2 && "grid-cols-2", columns === 3 && "grid-cols-3")}
      >
        {options.map((option) => {
          const checked = selected.includes(option);
          return (
            <label
              key={option}
              className={cn(
                "flex cursor-pointer items-center gap-2 rounded-md border px-3 py-2 text-sm transition-colors",
                checked
                  ? "border-primary bg-primary/5 text-foreground"
                  : "border-input text-muted-foreground hover:bg-accent",
              )}
            >
              <CheckboxPrimitive.Root
                checked={checked}
                onCheckedChange={() => handleToggle(option)}
                className={cn(
                  "flex size-4 shrink-0 items-center justify-center rounded-sm border transition-colors",
                  checked
                    ? "border-primary bg-primary text-primary-foreground"
                    : "border-muted-foreground",
                )}
              >
                <CheckboxPrimitive.Indicator>
                  <Check className="size-3" />
                </CheckboxPrimitive.Indicator>
              </CheckboxPrimitive.Root>
              {formatLabel(option)}
            </label>
          );
        })}
      </div>
    </div>
  );
}
