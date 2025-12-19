class CreateBookingRequest {
  final String propertyId;
  final String roomTypeId;
  final DateTime checkInDate;
  final DateTime checkOutDate;
  final int numGuests;
  final String guestEmail;
  final String guestFirstName;
  final String guestLastName;
  final String idempotencyKey;

  CreateBookingRequest({
    required this.propertyId,
    required this.roomTypeId,
    required this.checkInDate,
    required this.checkOutDate,
    required this.numGuests,
    required this.guestEmail,
    required this.guestFirstName,
    required this.guestLastName,
    required this.idempotencyKey,
  });

  Map<String, dynamic> toJson() => {
    'propertyId': propertyId,
    'roomTypeId': roomTypeId,
    'checkInDate': checkInDate.toIso8601String().split('T')[0],
    'checkOutDate': checkOutDate.toIso8601String().split('T')[0],
    'numGuests': numGuests,
    'guestEmail': guestEmail,
    'guestFirstName': guestFirstName,
    'guestLastName': guestLastName,
    'idempotencyKey': idempotencyKey,
  };
}

class BookingResponse {
  final String confirmationNumber;
  final String clientSecret;

  BookingResponse({
    required this.confirmationNumber,
    required this.clientSecret,
  });

  factory BookingResponse.fromJson(Map<String, dynamic> json) => BookingResponse(
    confirmationNumber: json['booking']['confirmationNumber'],
    clientSecret: json['clientSecret'],
  );
}
