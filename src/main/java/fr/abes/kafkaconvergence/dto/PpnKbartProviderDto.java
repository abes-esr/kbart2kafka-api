package fr.abes.kafkaconvergence.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PpnKbartProviderDto {
    private String ppn;
    private LigneKbartDto kbart;
    private String provider;

    @Override
    public String toString() {
        return "PPN : " + ppn + " / kbart : " + kbart.toString() + " / provider : " + provider;
    }

    @Override
    public int hashCode() {
        return this.ppn.hashCode() * this.kbart.hashCode() * this.provider.hashCode();
    }
}

