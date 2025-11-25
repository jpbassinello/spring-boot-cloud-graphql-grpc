package br.com.jpbassinello.sbcgg.mapstruct;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    uses = BaseMapper.class,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DefaultMapstructConfig {
}
