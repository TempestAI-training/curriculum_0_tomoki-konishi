import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://dmhsuhfcvtdijfzaxvpo.supabase.co'; // あなたのプロジェクトURL
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRtaHN1aGZjdnRkaWpmemF4dnBvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ0ODkwMzcsImV4cCI6MjA4MDA2NTAzN30.jPCvTVranlMLe1kFjC6Y5asUdZYgfuRJT8bVTLq_P2k';     // あなたのAPIキー
export const supabase = createClient(supabaseUrl, supabaseKey);