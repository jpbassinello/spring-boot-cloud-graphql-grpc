import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatLabel(value: string): string {
  return value
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/^\w/, (c) => c.toUpperCase());
}

const RELATIVE_TIME_FORMAT = new Intl.RelativeTimeFormat(undefined, { numeric: "auto" });

export function formatRelativeDate(iso: string | null | undefined): string | null {
  if (!iso) return null;
  const ms = new Date(iso).getTime();
  if (Number.isNaN(ms)) return null;
  const diffSec = Math.round((ms - Date.now()) / 1000);
  const abs = Math.abs(diffSec);
  if (abs < 60) return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / 1), "second");
  if (abs < 3600) return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / 60), "minute");
  if (abs < 86400) return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / 3600), "hour");
  if (abs < 86400 * 30) return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / 86400), "day");
  if (abs < 86400 * 365)
    return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / (86400 * 30)), "month");
  return RELATIVE_TIME_FORMAT.format(Math.round(diffSec / (86400 * 365)), "year");
}

export function formatAbsoluteDate(iso: string | null | undefined): string | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;
  return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
}
