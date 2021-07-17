package com.example.testapplicationweather.mapper

interface BaseMapper<E, D> {
    fun transformToDomain(type: E): D
    fun transformToDto(type: D): E
}