import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/search/presentation/search_screen.dart';
import '../../features/booking/presentation/availability_screen.dart';
import '../../features/booking/presentation/booking_details_screen.dart';
import '../../features/admin/presentation/admin_dashboard_screen.dart';

final appRouter = GoRouter(
  initialLocation: '/login',
  routes: [
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/search',
      builder: (context, state) => const SearchScreen(),
      routes: [
        GoRoute(
          path: 'availability/:propertyId',
          builder: (context, state) {
            final propertyId = state.pathParameters['propertyId']!;
            return AvailabilityScreen(propertyId: propertyId);
          },
        ),
      ],
    ),
    GoRoute(
      path: '/admin',
      builder: (context, state) => const AdminDashboardScreen(),
    ),
    GoRoute(
      path: '/booking/:roomTypeId',
      builder: (context, state) {
        final roomTypeId = state.pathParameters['roomTypeId']!;
        return BookingDetailsScreen(roomTypeId: roomTypeId);
      },
    ),
  ],
);
