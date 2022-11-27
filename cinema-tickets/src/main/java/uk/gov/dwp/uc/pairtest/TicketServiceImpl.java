package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Objects;


public class TicketServiceImpl implements TicketService {

    public static final int MAX_SEAT_ALLOWED_PER_REQUEST = 20;
    private final TicketPaymentService paymentService;

    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService paymentService,
                              SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }


    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
         if(!validRequest(ticketTypeRequests)){
             throw new InvalidPurchaseException();
         }
         int totalSeatRequired = calculateSeatsTotal(ticketTypeRequests);
         int totalAmount = calculateAmount(ticketTypeRequests);
         if(totalAmount>0) {
             paymentService.makePayment(accountId, totalAmount);
         }
        if(totalSeatRequired >0) {
             seatReservationService.reserveSeat(accountId, totalSeatRequired);
         }
    }

    private int calculateAmount(TicketTypeRequest[] ticketTypeRequests) {
        int totalAmount = 0;
        for (TicketTypeRequest ticketRequest :ticketTypeRequests) {
             int price = getPrice(ticketRequest.getTicketType());
             totalAmount += price * ticketRequest.getNoOfTickets();
        }
        return totalAmount;
    }

    private int getPrice(TicketTypeRequest.Type ticketType) {
        switch (ticketType){
            case ADULT:
                return 20;
            case CHILD:
                return  10;
            case INFANT:
                return 0;
        }
        return 0;
    }

    private int calculateSeatsTotal(TicketTypeRequest[] ticketTypeRequests) {
       return Arrays.stream(ticketTypeRequests)
                .filter(t->shouldAllocateSeat(t))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private boolean shouldAllocateSeat(TicketTypeRequest t) {
        return t.getTicketType() != TicketTypeRequest.Type.INFANT;
    }

    private boolean validRequest(TicketTypeRequest[] ticketTypeRequests) {
        return ticketTypeFieldValid(ticketTypeRequests)
                  && ticketTypeNotGreaterThanMaxAllowed(ticketTypeRequests, MAX_SEAT_ALLOWED_PER_REQUEST);
    }

    private boolean ticketTypeNotGreaterThanMaxAllowed(TicketTypeRequest[] ticketTypeRequests, int maxSeatAllowedPerRequest) {
        int totalSeat = Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
        return totalSeat <= MAX_SEAT_ALLOWED_PER_REQUEST;

    }

    private boolean ticketTypeFieldValid(TicketTypeRequest[] ticketTypeRequests) {
        return  Arrays.stream(ticketTypeRequests)
                   .allMatch(t -> t.getNoOfTickets() >=0
                            && Objects.nonNull(t.getTicketType()));
    }
}
