package uk.gov.hmcts.reform.amlib.internal.models;

public final class Service {
    private final String serviceName;
    private final String serviceDescription;


    public static class ServiceBuilder {
        private String serviceName;
        private String serviceDescription;

        ServiceBuilder() {
        }

        public ServiceBuilder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ServiceBuilder serviceDescription(final String serviceDescription) {
            this.serviceDescription = serviceDescription;
            return this;
        }

        public Service build() {
            return new Service(serviceName, serviceDescription);
        }

        @Override
        public String toString() {
            return "Service.ServiceBuilder(serviceName=" + this.serviceName + ", serviceDescription=" + this.serviceDescription + ")";
        }
    }

    public static ServiceBuilder builder() {
        return new ServiceBuilder();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getServiceDescription() {
        return this.serviceDescription;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Service)) return false;
        final Service other = (Service) o;
        final Object this$serviceName = this.getServiceName();
        final Object other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false;
        final Object this$serviceDescription = this.getServiceDescription();
        final Object other$serviceDescription = other.getServiceDescription();
        if (this$serviceDescription == null ? other$serviceDescription != null : !this$serviceDescription.equals(other$serviceDescription)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $serviceName = this.getServiceName();
        result = result * PRIME + ($serviceName == null ? 43 : $serviceName.hashCode());
        final Object $serviceDescription = this.getServiceDescription();
        result = result * PRIME + ($serviceDescription == null ? 43 : $serviceDescription.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Service(serviceName=" + this.getServiceName() + ", serviceDescription=" + this.getServiceDescription() + ")";
    }

    public Service(final String serviceName, final String serviceDescription) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
    }
}
