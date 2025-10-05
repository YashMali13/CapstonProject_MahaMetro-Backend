package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.WalletResponse;
import com.aurionpro.app.dto.WalletSummaryResponse;
import com.aurionpro.app.dto.WalletTransactionResponse;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(source = "user.id", target = "userId")
    WalletResponse toResponse(Wallet wallet);

    @Mapping(source = "user.id", target = "userId")
    WalletSummaryResponse toSummaryResponse(Wallet wallet);

    WalletTransactionResponse toTransactionResponse(WalletTransaction transaction);
}