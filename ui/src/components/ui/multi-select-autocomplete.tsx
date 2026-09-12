"use client";

import * as React from "react";
import { useCallback, useId, useState } from "react";
import { X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { formatLabel as defaultFormatLabel } from "@/lib/utils";

interface MultiSelectAutocompleteProps {
  label: string;
  placeholder: string;
  selected: string[];
  onSelectedChange: (selected: string[]) => void;
  onSearch?: (query: string) => void;
  suggestions: string[];
  minChars?: number;
  loading?: boolean;
  formatLabel?: (value: string) => string;
}

export function MultiSelectAutocomplete({
  label,
  placeholder,
  selected,
  onSelectedChange,
  onSearch,
  suggestions,
  minChars = 3,
  loading = false,
  formatLabel = defaultFormatLabel,
}: MultiSelectAutocompleteProps) {
  const [open, setOpen] = useState(false);
  const [inputValue, setInputValue] = useState("");
  const listboxId = useId();

  const handleInputChange = useCallback(
    (value: string) => {
      setInputValue(value);
      if (onSearch && (minChars === 0 || value.length >= minChars)) {
        onSearch(value);
      }
    },
    [minChars, onSearch],
  );

  const handleOpenChange = useCallback(
    (isOpen: boolean) => {
      setOpen(isOpen);
      if (isOpen && minChars === 0 && onSearch) {
        onSearch("");
      }
    },
    [minChars, onSearch],
  );

  const handleSelect = useCallback(
    (value: string) => {
      if (!selected.includes(value)) {
        onSelectedChange([...selected, value]);
      }
      setInputValue("");
    },
    [selected, onSelectedChange],
  );

  const handleRemove = useCallback(
    (value: string) => {
      onSelectedChange(selected.filter((s) => s !== value));
    },
    [selected, onSelectedChange],
  );

  const filteredSuggestions = suggestions.filter((s) => !selected.includes(s));

  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">{label}</label>
      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {selected.map((item) => (
            <Badge key={item} variant="secondary" className="gap-1">
              {formatLabel(item)}
              <button
                type="button"
                className="rounded-full outline-none hover:bg-muted"
                onClick={() => handleRemove(item)}
              >
                <X className="size-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
      <Popover open={open} onOpenChange={handleOpenChange}>
        <PopoverTrigger asChild>
          <button
            type="button"
            role="combobox"
            aria-expanded={open}
            aria-controls={listboxId}
            className="flex h-9 w-full items-center justify-between rounded-md border border-input bg-transparent px-3 py-1 text-sm text-muted-foreground shadow-xs hover:bg-accent"
          >
            {placeholder}
          </button>
        </PopoverTrigger>
        <PopoverContent
          id={listboxId}
          className="w-[--radix-popover-trigger-width] max-h-[min(20rem,var(--radix-popover-content-available-height))] p-0"
          align="start"
          side="bottom"
          avoidCollisions={false}
          collisionPadding={8}
        >
          <Command shouldFilter={minChars === 0}>
            <CommandInput
              placeholder={minChars === 0 ? "Filter..." : `Type ${minChars}+ characters...`}
              value={inputValue}
              onValueChange={handleInputChange}
            />
            <CommandList className="max-h-none flex-1 min-h-0">
              {minChars > 0 &&
                inputValue.length >= minChars &&
                filteredSuggestions.length === 0 &&
                !loading && (
                  <CommandEmpty className="px-3 py-3 text-center text-sm">
                    No results found.
                  </CommandEmpty>
                )}
              {minChars > 0 && inputValue.length < minChars && (
                <CommandEmpty className="px-3 py-3 text-center text-sm">
                  Type at least {minChars} characters to search.
                </CommandEmpty>
              )}
              {minChars === 0 && filteredSuggestions.length === 0 && !loading && (
                <CommandEmpty className="px-3 py-3 text-center text-sm">
                  No results found.
                </CommandEmpty>
              )}
              {loading && (
                <CommandEmpty className="px-3 py-3 text-center text-sm">Searching...</CommandEmpty>
              )}
              {filteredSuggestions.length > 0 && (
                <CommandGroup>
                  {filteredSuggestions.map((item) => (
                    <CommandItem
                      key={item}
                      value={item}
                      keywords={[formatLabel(item)]}
                      onSelect={handleSelect}
                    >
                      {formatLabel(item)}
                    </CommandItem>
                  ))}
                </CommandGroup>
              )}
            </CommandList>
          </Command>
        </PopoverContent>
      </Popover>
    </div>
  );
}
