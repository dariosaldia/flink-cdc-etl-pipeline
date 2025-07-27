package com.example.flinkcdc.pipeline;

import com.example.flinkcdc.model.CdcEvent;
import com.example.flinkcdc.model.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class OrdersPipelineTest {

    @Test
    void testIsUpsertWithUpsertOp() {
        CdcEvent evt = Mockito.mock(CdcEvent.class);
        Mockito.when(evt.op()).thenReturn("u");
        assertTrue(OrdersPipeline.isUpsert(evt), "Expected isUpsert to return true for op 'u'");
    }

    @Test
    void testIsUpsertWithDeleteOp() {
        CdcEvent evt = Mockito.mock(CdcEvent.class);
        Mockito.when(evt.op()).thenReturn("d");
        assertFalse(OrdersPipeline.isUpsert(evt), "Expected isUpsert to return false for op 'd'");
    }

    @Test
    void testToOrderReturnsAfter() {
        CdcEvent evt = Mockito.mock(CdcEvent.class);
        Order dummyOrder = Mockito.mock(Order.class);
        Mockito.when(evt.after()).thenReturn(dummyOrder);
        assertSame(dummyOrder, OrdersPipeline.toOrder(evt),
                "Expected toOrder to return the same Order instance from CdcEvent.after()");
    }
}
