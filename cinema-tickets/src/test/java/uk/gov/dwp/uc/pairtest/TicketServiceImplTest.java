package uk.gov.dwp.uc.pairtest;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private  TicketServiceImpl ticketService;


    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidInput_negativeNumberOfTickets(){
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
    }



    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidInput_nullInType(){
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(null, 1);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
    }


    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidInput_MaxTicketinSingleTicketType(){
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidInput_MaxTicketMultipleTicketType(){
        TicketTypeRequest ticketTypeRequest1 =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 7);
        TicketTypeRequest ticketTypeRequest2 =
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);
        TicketTypeRequest ticketTypeRequest3 =
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4);
        ticketService.purchaseTickets(100L, ticketTypeRequest1,ticketTypeRequest2,ticketTypeRequest3);
    }


    @Test
    public void testTotalCostAdultTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verify(ticketPaymentService).makePayment(100L, 140);
    }

    @Test
    public void testTotalCostChildTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verify(ticketPaymentService).makePayment(100L, 70);
    }

    @Test
    public void testTotalCostInfantTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verifyNoInteractions(ticketPaymentService);
    }

    @Test
    public void testTotalCost_1Adult_2Child_3Infants() {
        TicketTypeRequest ticketTypeRequest1 =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 =
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequest3 =
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        ticketService.purchaseTickets(100L, ticketTypeRequest1,ticketTypeRequest2,ticketTypeRequest3);
        Mockito.verify(ticketPaymentService).makePayment(100L, 40);
    }


    @Test
    public void testTotalSeats_1Adult_2Child_3Infants() {
        TicketTypeRequest ticketTypeRequest1 =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 =
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequest3 =
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        ticketService.purchaseTickets(100L, ticketTypeRequest1,ticketTypeRequest2,ticketTypeRequest3);
        Mockito.verify(seatReservationService).reserveSeat(100L, 3);
    }

    @Test
    public void testTotalSeatAdultTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verify(seatReservationService).reserveSeat(100L, 7);
    }

    @Test
    public void testTotalSeatChildTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verify(seatReservationService).reserveSeat(100L, 7);
    }

    @Test
    public void testTotalSeatInfantTickets() {
        TicketTypeRequest ticketTypeRequest =
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 7);
        ticketService.purchaseTickets(100L, ticketTypeRequest);
        Mockito.verifyNoInteractions(seatReservationService);
    }
}