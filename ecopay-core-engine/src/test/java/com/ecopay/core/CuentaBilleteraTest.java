package com.ecopay.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CuentaBilleteraTest {

    // 1. Crear cuenta válida
    @Test
    public void deberiaCrearCuentaValida() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 500.0, 100);
        assertEquals(500.0, c.getSaldo(), 0.01);
        assertEquals(100, c.getPuntosLealtad());
    }

    // 2. Error al crear cuenta con saldo negativo
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarCrearCuentaSaldoNegativo() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC1", "Juan", -50.0, 0);
    }

    // 3. Depositar monto válido
    @Test
    public void deberiaDepositar() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 500.0, 0);
        c.depositar(250.0);
        assertEquals(750.0, c.getSaldo(), 0.01);
    }

    // 4. Error al depositar monto negativo
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarDepositoNegativo() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC1", "Juan", 500.0, 0).depositar(-100.0);
    }

    // 5. Retirar saldo suficiente
    @Test
    public void deberiaRetirar() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 500.0, 0);
        c.retirar(300.0);
        assertEquals(200.0, c.getSaldo(), 0.01);
    }

    // 6. Error al retirar más del saldo (sobregiro)
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarRetiroMayorAlSaldo() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC1", "Juan", 500.0, 0).retirar(600.0);
    }

    // 7. Compra normal (puntos sin bono VIP)
    @Test
    public void deberiaAcumularPuntosSinBonoVIP() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 1000.0, 0);
        c.realizarCompra(255.0);
        assertEquals(745.0, c.getSaldo(), 0.01);
        assertEquals(25, c.getPuntosLealtad());
    }

    // 8. Compra VIP (puntos base + bono 50 pts)
    @Test
    public void deberiaAplicarBonoVIP() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 2000.0, 0);
        c.realizarCompra(1000.0);
        assertEquals(1000.0, c.getSaldo(), 0.01);
        assertEquals(150, c.getPuntosLealtad());
    }

    // 9. Transferencia usando Mockito
    @Test
    public void deberiaTransferirConMockito() throws TransaccionInvalidaException {
        CuentaBase destino = mock(CuentaBase.class);
        CuentaBilletera origen = new CuentaBilletera("ACC1", "Juan", 500.0, 0);
        origen.transferir(destino, 200.0);
        assertEquals(300.0, origen.getSaldo(), 0.01);
        verify(destino).depositar(200.0);
    }

    // 10. Atomicidad en transferencia fallida con Mockito
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaGarantizarAtomicidadConMockito() throws TransaccionInvalidaException {
        CuentaBase destino = mock(CuentaBase.class);
        CuentaBilletera origen = new CuentaBilletera("ACC1", "Juan", 100.0, 0);
        try {
            origen.transferir(destino, 300.0);
        } finally {
            verify(destino, never()).depositar(anyDouble());
        }
    }

    // 11. Canjear puntos por saldo
    @Test
    public void deberiaCanjearPuntosPorSaldo() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 100.0, 150);
        c.canjearPuntosPorSaldo(100);
        assertEquals(110.0, c.getSaldo(), 0.01);
        assertEquals(50, c.getPuntosLealtad());
    }

    // 12. Error al canjear puntos no múltiplos de 10
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarCanjeNoMultiploDeDiez() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC1", "Juan", 100.0, 50).canjearPuntosPorSaldo(25);
    }

    // 13. Error al transferir a destino nulo
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarTransferenciaACuentaNula() throws TransaccionInvalidaException {
        new CuentaBilletera("ACC1", "Juan", 500.0, 0).transferir(null, 100.0);
    }

    // 14. Error al transferir a sí misma
    @Test(expected = TransaccionInvalidaException.class)
    public void deberiaFallarAutoTransferencia() throws TransaccionInvalidaException {
        CuentaBilletera c = new CuentaBilletera("ACC1", "Juan", 500.0, 0);
        c.transferir(c, 100.0);
    }

    // 15. Probar interfaz con Mockito
    @Test
    public void deberiaProbarInterfazConMockito() throws TransaccionInvalidaException {
        OperableLealtad lealtad = mock(OperableLealtad.class);
        when(lealtad.calcularPuntosBase(250.0)).thenReturn(25);
        assertEquals(25, lealtad.calcularPuntosBase(250.0));
    }
}
