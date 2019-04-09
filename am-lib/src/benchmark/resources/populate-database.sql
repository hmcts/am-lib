INSERT INTO public.services (service_name, service_description) VALUES ('cmc', null) ON CONFLICT DO NOTHING;
INSERT INTO public.services (service_name, service_description) VALUES ('fpl', null) ON CONFLICT DO NOTHING;
